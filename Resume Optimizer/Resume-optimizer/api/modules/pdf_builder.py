"""
Build a formatted PDF resume from a structured data dict.
Mirrors the layout: centered name/contact, bold section headers with HR,
two-column org+date / role+location rows, bullet points, bold skill labels.
"""

import io
import re

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_RIGHT
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.units import inch
from reportlab.platypus import (
    HRFlowable,
    Paragraph,
    SimpleDocTemplate,
    Spacer,
    Table,
    TableStyle,
)


def _bold_labels(line: str) -> str:
    """Turn 'Label:' into '<b>Label:</b>' for skill lines."""
    return re.sub(r'([A-Za-z ,&]+):', r'<b>\1:</b>', line)


def build_resume_pdf(data: dict) -> bytes:
    buf = io.BytesIO()

    PAGE_W, _ = letter
    L_MAR = R_MAR = 0.75 * inch
    USABLE_W = PAGE_W - L_MAR - R_MAR
    COL_L = USABLE_W * 0.65
    COL_R = USABLE_W * 0.35

    doc = SimpleDocTemplate(
        buf,
        pagesize=letter,
        leftMargin=L_MAR,
        rightMargin=R_MAR,
        topMargin=0.55 * inch,
        bottomMargin=0.55 * inch,
    )

    # ── Styles ────────────────────────────────────────────────────────────
    def ps(name, font, size, leading, align=None, **kw):
        return ParagraphStyle(
            name,
            fontName=font,
            fontSize=size,
            leading=leading,
            alignment=align if align is not None else 0,
            **kw,
        )

    S_NAME    = ps('name',    'Times-Bold',   18, 22, TA_CENTER, spaceAfter=2)
    S_CONTACT = ps('contact', 'Times-Roman',  10, 13, TA_CENTER, spaceAfter=4)
    S_SEC     = ps('sec',     'Times-Bold',   11, 14, spaceBefore=6, spaceAfter=0)
    S_ORG     = ps('org',     'Times-Bold',   11, 14)
    S_DATE    = ps('date',    'Times-Roman',  10, 14, TA_RIGHT)
    S_ROLE    = ps('role',    'Times-Italic', 10, 13)
    S_LOC     = ps('loc',     'Times-Italic', 10, 13, TA_RIGHT)
    S_BULLET  = ps('bullet',  'Times-Roman',  10, 13, leftIndent=10, spaceAfter=1)
    S_NOTE    = ps('note',    'Times-Roman',  10, 13, spaceAfter=1)
    S_SKILL   = ps('skill',   'Times-Roman',  10, 13, spaceAfter=2)

    # ── Helpers ───────────────────────────────────────────────────────────
    _TS = TableStyle([
        ('VALIGN',        (0, 0), (-1, -1), 'TOP'),
        ('LEFTPADDING',   (0, 0), (-1, -1), 0),
        ('RIGHTPADDING',  (0, 0), (-1, -1), 0),
        ('TOPPADDING',    (0, 0), (-1, -1), 0),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 0),
    ])

    def two_col(left_para, right_para, space_before=0):
        t = Table(
            [[left_para, right_para]],
            colWidths=[COL_L, COL_R],
            spaceBefore=space_before,
        )
        t.setStyle(_TS)
        return t

    def hr():
        return HRFlowable(
            width='100%', thickness=0.75,
            color=colors.black, spaceBefore=2, spaceAfter=3,
        )

    # ── Build story ───────────────────────────────────────────────────────
    story = []

    # Header
    story.append(Paragraph(data.get('name', ''), S_NAME))
    story.append(Paragraph(data.get('contact', ''), S_CONTACT))

    skills_lines = data.get('skills_lines', [])

    for section in data.get('sections', []):
        title = section.get('title', '').upper().strip()
        if title == 'SKILLS':
            continue  # rendered below via skills_lines

        story.append(Paragraph(title, S_SEC))
        story.append(hr())

        for i, entry in enumerate(section.get('entries', [])):
            org       = (entry.get('org')       or '').strip()
            date      = (entry.get('date')      or '').strip()
            role      = (entry.get('role')      or '').strip()
            location  = (entry.get('location')  or '').strip()
            bullets   = entry.get('bullets') or []
            bold_note = (entry.get('bold_note') or '').strip()

            space = 5 if i > 0 else 0

            # org + date (or standalone bold line)
            if org and date:
                story.append(two_col(
                    Paragraph(org, S_ORG),
                    Paragraph(date, S_DATE),
                    space_before=space,
                ))
            elif org:
                # standalone line (e.g. Varsity Soccer entry)
                t = Table(
                    [[Paragraph(f'<b>{org}</b>', S_NOTE)]],
                    colWidths=[USABLE_W],
                    spaceBefore=space,
                )
                t.setStyle(_TS)
                story.append(t)

            # role + location
            if role:
                story.append(two_col(
                    Paragraph(role, S_ROLE),
                    Paragraph(location, S_LOC) if location else Paragraph('', S_LOC),
                ))

            # bullets
            for b in bullets:
                b = b.strip().lstrip('•-* ').strip()
                story.append(Paragraph(f'• {b}', S_BULLET))

            # bold note (Relevant Coursework, etc.)
            if bold_note:
                if ':' in bold_note:
                    lbl, rest = bold_note.split(':', 1)
                    html = f'<b>{lbl}:</b>{rest}'
                else:
                    html = bold_note
                story.append(Paragraph(html, S_NOTE))

    # Skills
    if skills_lines:
        story.append(Paragraph('SKILLS', S_SEC))
        story.append(hr())
        for line in skills_lines:
            if isinstance(line, dict):
                lbl  = line.get('label', '')
                text = line.get('text', '')
                html = f'<b>{lbl}:</b> {text}' if lbl else text
            else:
                html = _bold_labels(str(line))
            story.append(Paragraph(html, S_SKILL))

    doc.build(story)
    buf.seek(0)
    return buf.getvalue()
