from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity


def _keyword_match(keyword: str, resume_lower: str) -> bool:
    return keyword.lower() in resume_lower


def _tfidf_score(resume_text: str, keyword_list: list[str]) -> int:
    jd_combined = " ".join(keyword_list)
    docs = [resume_text or " ", jd_combined or " "]
    try:
        vectorizer = TfidfVectorizer(stop_words="english", ngram_range=(1, 2))
        matrix = vectorizer.fit_transform(docs)
        sim = cosine_similarity(matrix[0:1], matrix[1:2])[0][0]
        return int(float(sim) * 100)
    except Exception:
        return 0


def compute_match_score(resume_text: str, jd_keywords: dict) -> dict:
    resume_lower = resume_text.lower()

    all_keywords: list[str] = []
    all_keywords.extend(jd_keywords.get("must_have", []))
    all_keywords.extend(jd_keywords.get("nice_to_have", []))
    all_keywords.extend(jd_keywords.get("skills", []))

    matched: list[str] = []
    missing: list[str] = []

    for kw in all_keywords:
        if _keyword_match(kw, resume_lower):
            matched.append(kw)
        else:
            missing.append(kw)

    keyword_score = int(len(matched) / len(all_keywords) * 100) if all_keywords else 0
    tfidf = _tfidf_score(resume_text, all_keywords)
    overall_score = int(keyword_score * 0.6 + tfidf * 0.4)

    must_have = jd_keywords.get("must_have", [])
    weak: list[str] = []
    for kw in must_have:
        if kw in matched and resume_lower.count(kw.lower()) == 1:
            weak.append(kw)

    return {
        "overall_score": overall_score,
        "matched": matched,
        "missing": missing,
        "weak": weak,
    }
