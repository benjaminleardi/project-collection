# Resume Keyword Optimizer

ATS match scoring and AI-powered bullet rewrites · Ψ Eta Mu capstone.

## Stack

| Layer | Tech |
|-------|------|
| Frontend | Vanilla HTML/CSS/JS — zero dependencies |
| Backend  | FastAPI (Python) as a Vercel serverless function |
| Parsing  | pdfplumber + python-docx |
| Scoring  | scikit-learn TF-IDF + keyword match |
| AI       | Anthropic claude-sonnet-4-5 (2 calls per analysis) |

---

## Deploy to Vercel (< 5 minutes)

```bash
# 1 — push repo to GitHub
git add . && git commit -m "init" && git push

# 2 — install Vercel CLI (if needed)
npm i -g vercel

# 3 — deploy
cd resume-optimizer
vercel

# 4 — set the API key as an environment variable
vercel env add ANTHROPIC_API_KEY
# paste your sk-ant-... key when prompted, select all environments
```

Or via the Vercel dashboard:
1. Import your GitHub repo at vercel.com/new
2. Leave all build settings as-is (vercel.json handles routing)
3. Add `ANTHROPIC_API_KEY` under **Settings → Environment Variables**
4. Redeploy

---

## Local development

```bash
git clone <your-repo>
cd resume-optimizer

python3 -m venv .venv
source .venv/bin/activate          # Windows: .venv\Scripts\activate

pip install -r requirements.txt

cp .env.example .env               # add your key

uvicorn api.index:app --reload     # → http://localhost:8000
```

---

## Run tests

```bash
python3 -m pytest tests/test_scoring.py -v
```

---

## Project structure

```
api/
  index.py          FastAPI app — serves HTML and /api/analyze
public/
  index.html        Single-page frontend (HTML + CSS + JS, no framework)
modules/
  parsers.py        PDF / DOCX / text extraction
  prompts.py        Claude prompt templates
  llm.py            extract_keywords_from_jd + suggest_bullet_rewrites
  scoring.py        TF-IDF cosine + keyword match → overall_score
tests/
  sample_resume.txt
  sample_jd.txt
  test_scoring.py   7 unit tests (no API key needed)
vercel.json         Routes all traffic to api/index.py
requirements.txt
.env.example
```

---

## API

### `POST /api/analyze`

`multipart/form-data` fields:

| Field | Type | Required |
|-------|------|----------|
| `jd_text` | string | ✅ |
| `resume_text` | string | one of these |
| `resume_file` | file (PDF/DOCX/TXT) | one of these |

Response:
```json
{
  "overall_score": 72,
  "matched": ["Python", "SQL", "machine learning"],
  "missing": ["Spark", "MLflow", "A/B testing"],
  "weak": ["scikit-learn"],
  "rewrites": [
    {
      "original": "Analyzed datasets using Python",
      "rewrite": "Analyzed large datasets using Python and Spark, applying A/B testing methodologies to validate findings",
      "keywords_added": ["Spark", "A/B testing"]
    }
  ],
  "jd_keywords": {
    "must_have": [...],
    "nice_to_have": [...],
    "skills": [...]
  }
}
```

---

## Team workflow

| Module | Owner |
|--------|-------|
| `modules/parsers.py` | Member A — no API needed |
| `modules/prompts.py` + `llm.py` | Member B — tune prompts |
| `modules/scoring.py` | Member C — unit-testable |
| `public/index.html` UI | Member D — open with Live Server |

Everyone runs `uvicorn api.index:app --reload` locally to see their changes end-to-end.

## Vercel limits (Hobby plan)

Two Claude API calls typically finish in 15–30 s, well under Vercel's 60 s function timeout. If you're seeing timeouts, upgrade to the Pro plan (300 s limit).
