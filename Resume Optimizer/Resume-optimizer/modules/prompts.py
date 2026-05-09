JD_KEYWORD_EXTRACTION = """\
Analyze the job description below and extract keywords into three categories.

Return ONLY a valid JSON object with no extra text, markdown, or explanation:

{{
  "must_have": ["required qualifications, certifications, non-negotiable skills"],
  "nice_to_have": ["preferred or bonus qualifications"],
  "skills": ["specific technical tools, languages, software, domain expertise"]
}}

Rules:
- Keep each keyword concise (1-4 words).
- Extract 5-15 items per category.
- Do not duplicate items across categories.

Job Description:
{jd_text}"""


BULLET_REWRITE = """\
You are a resume vocabulary optimizer. Your only job is to rephrase existing \
bullet points using more precise, ATS-friendly language. You are NOT a resume \
writer and you do NOT add content.

{role_context}

ABSOLUTE CONSTRAINTS — violating any one is a failure:

1. NEVER add experience, skills, tools, accomplishments, or responsibilities \
that are not directly stated or performed in the original bullet.
2. NEVER upgrade scope. If the original says "assisted", do not write "led". \
If it says "small dataset", do not write "large-scale data".
3. NEVER add a technology or tool unless it is named in the original bullet.
4. Preserve every number, percentage, and dollar amount exactly as written.
5. If a missing keyword cannot be incorporated without violating rules 1-3, \
leave that bullet unchanged — set "rewrite" = "original" and "keywords_added" = [].
6. Every rewritten bullet must start with a strong action verb already \
implied by the original.

WHAT IS ALLOWED:
- Substituting a vague term for a precise one that means the same thing.
  Example: "worked with data" -> "performed exploratory data analysis"
- Reordering phrases to surface a keyword earlier.
- Adding a clarifying adjective that is directly supported by the original context.
  Example: "built a pipeline" -> "built an automated ETL pipeline" \
(only if automation is implied by the original description)

WHAT IS FORBIDDEN:
- Original: "Assisted the data team with reporting"
  FORBIDDEN rewrite: "Led data-driven reporting initiatives using SQL and Tableau"
  (invented leadership, invented tools)
  ALLOWED rewrite: "Assisted the data team with data-driven reporting"
  (only adds the adjective, keeps the same scope)

SELF-CHECK before finalizing each rewrite:
Ask: "Does this rewrite claim anything — a tool, a skill, a scope, a \
responsibility — not directly present in the original bullet?"
If yes, remove it.

Missing keywords to incorporate where genuinely applicable:
{missing_keywords}

Return ONLY a valid JSON array with no extra text:
[
  {{
    "original": "original bullet text",
    "rewrite": "improved bullet text (or identical to original if no safe change exists)",
    "keywords_added": ["only keywords actually woven in without fabrication"]
  }}
]

Bullet points to improve:
{bullets}"""
