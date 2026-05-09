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


RESUME_OPTIMIZE = """\
You are a professional resume editor. Produce a concise, ATS-optimized, \
one-page resume from the content below. Do NOT invent anything.

ABSOLUTE RULES — no exceptions:
1. Do not add any experience, skills, tools, responsibilities, or accomplishments \
not present in the original resume.
2. Do not change any numbers, percentages, dates, company names, or job titles.
3. Do not upgrade scope — if the original says "assisted", keep "assisted".
4. Do not add a technology or tool unless it already appears in the original.
5. Weave in the missing keywords ONLY where they fit naturally — never force them.
6. Fix weak action verbs and passive voice throughout.
7. Enforce parallel structure within each bullet list.
8. Consistent tense: past for ended roles, present for current roles.
9. Remove all filler words and clichés: "responsible for", "helped with", \
"worked on", "various", "multiple", "leveraged", "utilized", "dynamic", \
"detail-oriented", "team player", "passionate about", "results-driven".

CONCISENESS — this must fit on ONE page:
- Maximum 2 strong bullet points per role (pick the most impactful).
- Each bullet: one tight sentence, starts with an action verb, no filler.
- Relevant Coursework: max 4 courses.
- Summary: exactly 2 sentences synthesized only from content already in the resume.

SECTION ORDER (use exactly these titles, in this order):
1. SUMMARY  (synthesize from existing content — do not fabricate)
2. EXPERIENCE
3. PROJECTS  (include only if present in original)
4. EDUCATION
5. LEADERSHIP & ACTIVITIES  (include only if present in original; max 2 entries)
6. SKILLS   (via skills_lines — do NOT include in sections[])

SELF-CHECK: scan every entry — if any new skill, tool, job title, or \
accomplishment appears that was not in the original, remove it.

Missing keywords to incorporate where genuinely applicable:
{missing_keywords}

Return ONLY a valid JSON object — no markdown fences, no extra text:
{{
  "name": "Full Name",
  "contact": "Address | Phone | Email | LinkedIn",
  "summary": "Exactly 2 sentences. Synthesized from existing resume content only.",
  "sections": [
    {{
      "title": "EXPERIENCE",
      "entries": [
        {{
          "org": "Company Name",
          "date": "Month Year–Month Year",
          "role": "Job Title",
          "location": "City, ST",
          "bullets": ["Strong bullet one.", "Strong bullet two."],
          "bold_note": null
        }}
      ]
    }},
    {{
      "title": "PROJECTS",
      "entries": [
        {{
          "org": "Project Name",
          "date": "Year",
          "role": "Role Title",
          "location": "",
          "bullets": ["Strong bullet one.", "Strong bullet two."],
          "bold_note": null
        }}
      ]
    }},
    {{
      "title": "EDUCATION",
      "entries": [
        {{
          "org": "University Name",
          "date": "Expected Month Year",
          "role": "Degree Program",
          "location": "City, ST",
          "bullets": [],
          "bold_note": "Relevant Coursework: Course 1, Course 2, Course 3, Course 4"
        }}
      ]
    }}
  ],
  "skills_lines": [
    "Languages: Python, JavaScript | Data & Analytics: Excel, Pandas | AI Tools: Cursor, Claude Code, LangFlow",
    "Tools: Microsoft Office, Canva | Languages Spoken: Fluent Malayalam | Conversational French"
  ]
}}

Schema rules:
- "summary" is a top-level string field — do NOT put SUMMARY inside sections[].
- Do NOT include a SKILLS entry inside sections[] — use skills_lines[] only.
- skills_lines[] is an array of plain strings using "Label: items | Label: items" format.
- For standalone lines with no role/date (e.g. "Varsity Soccer — ..."), \
  set org to the full line and leave date, role, location as "".
- bold_note is for lines like "Relevant Coursework: ..." — null if none.
- bullets[] contains text only, no leading bullet character.

Original Resume:
{resume_text}"""
