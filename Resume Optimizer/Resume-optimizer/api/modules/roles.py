ROLE_PROFILES: dict[str, dict] = {
    "data_science": {
        "label": "Data Science",
        "emoji": "📊",
        "must_have": [
            "machine learning",
            "statistical modeling",
            "A/B testing",
            "hypothesis testing",
            "feature engineering",
            "model deployment",
            "data pipeline",
            "experimental design",
            "exploratory data analysis",
            "predictive modeling",
        ],
        "nice_to_have": [
            "MLflow",
            "experiment tracking",
            "causal inference",
            "time series forecasting",
            "NLP",
            "deep learning",
            "LLM",
            "model monitoring",
        ],
        "skills": [
            "Python",
            "SQL",
            "scikit-learn",
            "pandas",
            "NumPy",
            "TensorFlow",
            "PyTorch",
            "Spark",
            "Tableau",
            "Git",
            "AWS",
            "Docker",
            "Jupyter",
            "XGBoost",
        ],
        "rewrite_style": (
            "Emphasize quantified model outcomes (accuracy gains, AUC, lift, revenue impact). "
            "Highlight scale of data handled and end-to-end ownership from ingestion to production. "
            "Use precise ML vocabulary: cross-validation, hyperparameter tuning, production inference."
        ),
    },
    "swe": {
        "label": "Software Engineering",
        "emoji": "💻",
        "must_have": [
            "system design",
            "algorithms",
            "data structures",
            "code review",
            "CI/CD",
            "REST API",
            "microservices",
            "unit testing",
            "scalability",
            "agile",
            "version control",
        ],
        "nice_to_have": [
            "distributed systems",
            "Kubernetes",
            "GraphQL",
            "event-driven architecture",
            "observability",
            "load balancing",
            "caching",
            "message queue",
        ],
        "skills": [
            "Python",
            "Java",
            "JavaScript",
            "TypeScript",
            "React",
            "Node.js",
            "SQL",
            "NoSQL",
            "AWS",
            "Docker",
            "Git",
            "PostgreSQL",
            "Redis",
            "Kafka",
        ],
        "rewrite_style": (
            "Lead with scale and system impact: requests/sec, latency reduction, uptime SLA. "
            "Name the specific component, the technical problem solved, and the measurable outcome. "
            "Use engineering precision — avoid vague verbs like 'worked on'; prefer 'designed', "
            "'reduced', 'automated', 'migrated'."
        ),
    },
    "product_management": {
        "label": "Product Management",
        "emoji": "🗺️",
        "must_have": [
            "product roadmap",
            "user stories",
            "stakeholder management",
            "A/B testing",
            "product strategy",
            "go-to-market",
            "OKRs",
            "user research",
            "product lifecycle",
            "cross-functional collaboration",
            "requirements gathering",
            "prioritization",
        ],
        "nice_to_have": [
            "PRD",
            "market sizing",
            "competitive analysis",
            "customer discovery",
            "product-market fit",
            "growth metrics",
            "north star metric",
        ],
        "skills": [
            "JIRA",
            "Confluence",
            "Figma",
            "SQL",
            "Mixpanel",
            "Amplitude",
            "Looker",
            "Google Analytics",
            "Notion",
        ],
        "rewrite_style": (
            "Frame every bullet around user impact and business outcome. "
            "Lead with the problem, then the product decision, then the metric result "
            "(DAU, retention rate, conversion lift, NPS). "
            "Show ownership of the full product lifecycle: discovery → spec → launch → iteration."
        ),
    },
    "consulting": {
        "label": "Consulting",
        "emoji": "📈",
        "must_have": [
            "client engagement",
            "stakeholder management",
            "executive presentations",
            "data-driven insights",
            "workstream management",
            "hypothesis-driven",
            "problem structuring",
            "due diligence",
            "strategic recommendations",
            "cross-functional collaboration",
            "project management",
        ],
        "nice_to_have": [
            "MECE",
            "market entry",
            "operational efficiency",
            "post-merger integration",
            "change management",
            "cost reduction",
            "benchmarking",
        ],
        "skills": [
            "Excel",
            "PowerPoint",
            "financial modeling",
            "market sizing",
            "SQL",
            "Tableau",
            "Python",
        ],
        "rewrite_style": (
            "Use consulting narrative structure: business problem → structured approach → quantified client outcome. "
            "Anchor every bullet to a dollar figure, percentage improvement, or headcount impact. "
            "Prefer verbs like 'advised', 'synthesized', 'structured', 'presented to C-suite', 'drove'. "
            "Avoid internal jargon — write as if the audience is a partner reading your case team's slide."
        ),
    },
}
