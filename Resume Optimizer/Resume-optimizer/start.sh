#!/bin/bash
cd "/Users/rehanhashim/PHM Capstone/resume-optimizer"
.venv/bin/uvicorn api.index:app --port "${PORT:-8000}"
