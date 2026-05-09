import asyncio
import io
import os
import sys
from pathlib import Path

# modules/ and templates/ live next to this file inside api/
_HERE = Path(__file__).parent
sys.path.insert(0, str(_HERE))

from dotenv import load_dotenv
load_dotenv()

from fastapi import FastAPI, File, Form, HTTPException, UploadFile
from fastapi.responses import HTMLResponse, Response

app = FastAPI(title="Resume Keyword Optimizer")

_HTML_PATH = _HERE / "templates" / "index.html"


@app.get("/", response_class=HTMLResponse)
async def serve_ui() -> HTMLResponse:
    return HTMLResponse(content=_HTML_PATH.read_text(encoding="utf-8"))


@app.post("/api/analyze")
async def analyze(
    jd_text: str = Form(...),
    resume_text: str = Form(""),
    resume_file: UploadFile = File(None),
    role: str = Form(""),
):
    api_key = os.environ.get("ANTHROPIC_API_KEY", "")
    if not api_key:
        raise HTTPException(
            status_code=500,
            detail="ANTHROPIC_API_KEY is not configured on the server.",
        )

    from modules.parsers import clean_text, parse_docx, parse_pdf
    from modules.roles import ROLE_PROFILES

    if resume_file and resume_file.filename:
        raw_bytes = await resume_file.read()
        ext = resume_file.filename.rsplit(".", 1)[-1].lower()
        if ext == "pdf":
            resume_content = parse_pdf(io.BytesIO(raw_bytes))
        elif ext in ("doc", "docx"):
            resume_content = parse_docx(io.BytesIO(raw_bytes))
        else:
            resume_content = raw_bytes.decode("utf-8", errors="replace")
    else:
        resume_content = resume_text

    resume_content = clean_text(resume_content)

    if not resume_content.strip():
        raise HTTPException(
            status_code=400,
            detail="Resume content is empty. Upload a file or paste your resume text.",
        )
    if not jd_text.strip():
        raise HTTPException(status_code=400, detail="Job description cannot be empty.")

    from modules.llm import extract_keywords_from_jd, suggest_bullet_rewrites
    from modules.scoring import compute_match_score

    # Step 1: keyword extraction (must finish before scoring)
    try:
        jd_keywords = await asyncio.to_thread(extract_keywords_from_jd, jd_text)
    except Exception as exc:
        raise HTTPException(status_code=502, detail=f"Keyword extraction failed: {exc}")

    role_context = ""
    if role and role in ROLE_PROFILES:
        jd_keywords = _merge_role_keywords(jd_keywords, ROLE_PROFILES[role])
        role_context = ROLE_PROFILES[role]["rewrite_style"]

    score_data = compute_match_score(resume_content, jd_keywords)

    # Step 2: bullet rewrites — run in background, skip if it fails or times out
    rewrites: list[dict] = []
    if score_data["missing"]:
        bullets = _extract_bullets(resume_content)
        if bullets:
            try:
                raw_rewrites = await asyncio.wait_for(
                    asyncio.to_thread(
                        suggest_bullet_rewrites,
                        bullets,
                        score_data["missing"][:10],
                        role_context,
                    ),
                    timeout=7.0,  # hard cap — skip rewrites if Claude is slow
                )
                rewrites = [
                    r for r in raw_rewrites
                    if r.get("rewrite") and r.get("rewrite") != r.get("original", "")
                ]
            except Exception:
                rewrites = []  # non-fatal — score + keywords still returned

    return {
        "overall_score": score_data["overall_score"],
        "matched": score_data["matched"],
        "missing": score_data["missing"],
        "weak": score_data["weak"],
        "rewrites": rewrites,
        "jd_keywords": jd_keywords,
        "role": role,
        "resume_text": resume_content,
    }


@app.post("/api/optimize")
async def optimize_resume(
    resume_text: str = Form(...),
    missing_keywords: str = Form(""),
):
    api_key = os.environ.get("ANTHROPIC_API_KEY", "")
    if not api_key:
        raise HTTPException(status_code=500, detail="ANTHROPIC_API_KEY not configured.")

    from modules.llm import generate_optimized_resume

    keywords = [k.strip() for k in missing_keywords.split(",") if k.strip()]

    try:
        resume_data = await asyncio.to_thread(generate_optimized_resume, resume_text, keywords)
    except Exception as exc:
        raise HTTPException(status_code=502, detail=f"Resume optimization failed: {exc}")

    return {"resume_data": resume_data}


def _merge_role_keywords(jd_keywords: dict, profile: dict) -> dict:
    merged = {k: list(v) for k, v in jd_keywords.items()}
    for category in ("must_have", "nice_to_have", "skills"):
        existing = {k.lower() for k in merged.get(category, [])}
        for kw in profile.get(category, []):
            if kw.lower() not in existing:
                merged.setdefault(category, []).append(kw)
    return merged


def _extract_bullets(resume_text: str) -> list[str]:
    BULLET_CHARS = set("-•*–▪◦")
    bullets: list[str] = []
    for line in resume_text.split("\n"):
        stripped = line.strip()
        if len(stripped) < 30:
            continue
        if stripped[0] in BULLET_CHARS:
            clean = stripped.lstrip("".join(BULLET_CHARS) + " ").strip()
            if clean:
                bullets.append(clean)
    return bullets[:20]
