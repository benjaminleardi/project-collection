import os

import streamlit as st
from dotenv import load_dotenv

load_dotenv()

try:
    if not os.environ.get("ANTHROPIC_API_KEY") and "ANTHROPIC_API_KEY" in st.secrets:
        os.environ["ANTHROPIC_API_KEY"] = st.secrets["ANTHROPIC_API_KEY"]
except Exception:
    pass

st.set_page_config(
    page_title="Resume Keyword Optimizer",
    page_icon="📄",
    layout="wide",
)


@st.cache_data(show_spinner=False)
def cached_extract_keywords(jd_text: str) -> dict:
    from modules.llm import extract_keywords_from_jd
    return extract_keywords_from_jd(jd_text)


@st.cache_data(show_spinner=False)
def cached_suggest_rewrites(
    bullets_tuple: tuple[str, ...], missing_tuple: tuple[str, ...]
) -> list[dict]:
    from modules.llm import suggest_bullet_rewrites
    return suggest_bullet_rewrites(list(bullets_tuple), list(missing_tuple))


def get_resume_text(uploaded_file, pasted_text: str) -> str:
    if uploaded_file is not None:
        ext = uploaded_file.name.rsplit(".", 1)[-1].lower()
        if ext == "pdf":
            from modules.parsers import parse_pdf
            return parse_pdf(uploaded_file)
        if ext in ("doc", "docx"):
            from modules.parsers import parse_docx
            return parse_docx(uploaded_file)
        return uploaded_file.read().decode("utf-8", errors="replace")
    return pasted_text


def extract_bullets(resume_text: str) -> list[str]:
    BULLET_CHARS = set("-•*–▪◦")
    lines = resume_text.split("\n")
    bullets: list[str] = []
    for line in lines:
        stripped = line.strip()
        if len(stripped) < 30:
            continue
        if stripped[0] in BULLET_CHARS:
            clean = stripped.lstrip("".join(BULLET_CHARS) + " ").strip()
            if clean:
                bullets.append(clean)
    return bullets[:20]


def render_score_circle(score: int) -> None:
    if score >= 70:
        color = "#22c55e"
    elif score >= 40:
        color = "#f59e0b"
    else:
        color = "#ef4444"
    html = (
        f'<div style="display:flex;justify-content:center;margin:1.5rem 0;">'
        f'<div style="width:150px;height:150px;border-radius:50%;'
        f'border:10px solid {color};display:flex;flex-direction:column;'
        f'align-items:center;justify-content:center;">'
        f'<span style="font-size:2.8rem;font-weight:700;color:{color};">{score}</span>'
        f'<span style="font-size:0.75rem;color:#6b7280;margin-top:-4px;">ATS Score</span>'
        f"</div></div>"
    )
    st.markdown(html, unsafe_allow_html=True)


def render_pills(keywords: list[str], color: str) -> None:
    if not keywords:
        st.caption("None found.")
        return
    pill_style = (
        f"background:{color};color:white;padding:4px 12px;"
        "border-radius:999px;font-size:0.8rem;margin:3px;display:inline-block;"
    )
    pills = " ".join(
        f'<span style="{pill_style}">{kw}</span>' for kw in keywords
    )
    st.markdown(pills, unsafe_allow_html=True)


def main() -> None:
    st.title("Resume Keyword Optimizer")
    st.caption(
        "Upload or paste your resume and a job description to get an ATS match score, "
        "missing keywords, and AI-suggested bullet rewrites."
    )

    col_resume, col_jd = st.columns(2)

    with col_resume:
        st.subheader("Your Resume")
        uploaded_file = st.file_uploader(
            "Upload PDF, DOCX, or TXT", type=["pdf", "docx", "doc", "txt"]
        )
        pasted_resume = st.text_area(
            "Or paste resume text", height=280, key="resume_paste"
        )

    with col_jd:
        st.subheader("Job Description")
        jd_text = st.text_area(
            "Paste the full job description", height=340, key="jd_paste"
        )

    analyze = st.button("Analyze", type="primary", use_container_width=True)

    if not analyze:
        return

    resume_text = get_resume_text(uploaded_file, pasted_resume)

    if not resume_text or not resume_text.strip():
        st.error("Please upload a resume file or paste your resume text.")
        return

    if not jd_text or not jd_text.strip():
        st.error("Please paste a job description.")
        return

    if not os.environ.get("ANTHROPIC_API_KEY"):
        st.error(
            "ANTHROPIC_API_KEY is not configured. "
            "Add it to your .env file (local) or Streamlit Cloud secrets."
        )
        return

    from modules.parsers import clean_text
    from modules.scoring import compute_match_score

    resume_clean = clean_text(resume_text)

    with st.spinner("Extracting keywords from job description..."):
        try:
            jd_keywords = cached_extract_keywords(jd_text)
        except ValueError as exc:
            st.error(str(exc))
            return
        except Exception as exc:
            st.error(f"Keyword extraction failed: {exc}")
            return

    score_data = compute_match_score(resume_clean, jd_keywords)

    st.divider()
    render_score_circle(score_data["overall_score"])

    col_left, col_right = st.columns(2)

    with col_left:
        st.markdown("**Matched Keywords**")
        render_pills(score_data["matched"], "#22c55e")

    with col_right:
        st.markdown("**Missing Keywords**")
        render_pills(score_data["missing"], "#ef4444")

    if score_data["weak"]:
        st.markdown("**Keywords to Strengthen** *(present but underemphasized)*")
        render_pills(score_data["weak"], "#f59e0b")

    st.divider()

    if not score_data["missing"]:
        st.success(
            "Your resume already contains all key terms from this job description."
        )
        return

    st.subheader("AI Bullet Rewrite Suggestions")

    bullets = extract_bullets(resume_clean)

    if not bullets:
        st.info(
            "No bullet points detected. Make sure your resume uses lines that start "
            "with -, •, *, –, or ▪ to get rewrite suggestions."
        )
        return

    with st.spinner("Generating rewrite suggestions (this may take ~15 seconds)..."):
        try:
            rewrites = cached_suggest_rewrites(
                tuple(bullets),
                tuple(score_data["missing"][:10]),
            )
        except Exception as exc:
            st.error(f"Rewrite generation failed: {exc}")
            return

    changed = [r for r in rewrites if r.get("rewrite") != r.get("original", "")]

    if not changed:
        st.info("No rewrite opportunities found for the detected bullets.")
        return

    for item in changed:
        original = item.get("original", "")
        label = original[:77] + "..." if len(original) > 80 else original
        with st.expander(f"**Before:** {label}"):
            st.markdown(f"**After:** {item.get('rewrite', '')}")
            added = item.get("keywords_added", [])
            if added:
                st.markdown("**Keywords added:**")
                render_pills(added, "#6366f1")


if __name__ == "__main__":
    main()
