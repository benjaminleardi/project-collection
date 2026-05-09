import json
import os

from modules.prompts import BULLET_REWRITE, JD_KEYWORD_EXTRACTION, RESUME_OPTIMIZE


def _get_client():
    from anthropic import Anthropic
    api_key = os.environ.get("ANTHROPIC_API_KEY", "")
    if not api_key:
        raise ValueError("ANTHROPIC_API_KEY is not set.")
    return Anthropic(api_key=api_key)


def _parse_json(raw: str):
    raw = raw.strip()
    if raw.startswith("```"):
        lines = raw.splitlines()
        lines = [ln for ln in lines if not ln.strip().startswith("```")]
        raw = "\n".join(lines)
    return json.loads(raw)


FAST_MODEL = "claude-haiku-4-5"   # fast model — used for analyze
FULL_MODEL = "claude-sonnet-4-5"  # full model — used for resume rewrite only


def extract_keywords_from_jd(jd_text: str) -> dict:
    client = _get_client()
    prompt = JD_KEYWORD_EXTRACTION.format(jd_text=jd_text)
    message = client.messages.create(
        model=FAST_MODEL,
        max_tokens=1024,
        messages=[{"role": "user", "content": prompt}],
    )
    result = _parse_json(message.content[0].text)
    result.setdefault("must_have", [])
    result.setdefault("nice_to_have", [])
    result.setdefault("skills", [])
    return result


def suggest_bullet_rewrites(
    resume_bullets: list[str],
    missing_keywords: list[str],
    role_context: str = "",
) -> list[dict]:
    client = _get_client()
    bullets_formatted = "\n".join(f"- {b}" for b in resume_bullets)
    keywords_str = ", ".join(missing_keywords)
    role_block = f"\nROLE STYLE GUIDE: {role_context}\n" if role_context else ""
    prompt = BULLET_REWRITE.format(
        role_context=role_block,
        missing_keywords=keywords_str,
        bullets=bullets_formatted,
    )
    message = client.messages.create(
        model=FAST_MODEL,
        max_tokens=2048,
        messages=[{"role": "user", "content": prompt}],
    )
    return _parse_json(message.content[0].text)


def generate_optimized_resume(
    resume_text: str,
    missing_keywords: list[str],
) -> dict:
    """Return structured resume data dict; PDF is built client-side."""
    client = _get_client()
    keywords_str = ", ".join(missing_keywords[:20])
    prompt = RESUME_OPTIMIZE.format(
        missing_keywords=keywords_str,
        resume_text=resume_text,
    )
    message = client.messages.create(
        model="claude-sonnet-4-5",
        max_tokens=5000,
        messages=[{"role": "user", "content": prompt}],
    )
    return _parse_json(message.content[0].text)
