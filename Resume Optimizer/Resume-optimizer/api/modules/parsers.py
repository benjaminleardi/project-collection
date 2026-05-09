import re


def parse_pdf(file) -> str:
    import pdfplumber
    text_parts = []
    with pdfplumber.open(file) as pdf:
        for page in pdf.pages:
            page_text = page.extract_text()
            if page_text:
                text_parts.append(page_text)
    return "\n".join(text_parts)


def parse_docx(file) -> str:
    from docx import Document
    doc = Document(file)
    paragraphs = []
    for para in doc.paragraphs:
        if para.text.strip():
            paragraphs.append(para.text)
    return "\n".join(paragraphs)


def clean_text(raw: str) -> str:
    lines = raw.split("\n")
    cleaned = []
    for line in lines:
        line = re.sub(r"[\x00-\x08\x0b\x0c\x0e-\x1f\x7f]", "", line)
        line = re.sub(r"[ \t]+", " ", line)
        cleaned.append(line.strip())
    return "\n".join(cleaned).strip()
