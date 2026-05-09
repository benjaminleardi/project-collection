import os
import sys
import unittest

sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))

from modules.scoring import compute_match_score


RESUME = """
Experienced data scientist with expertise in Python and machine learning.
Developed predictive models using scikit-learn and TensorFlow.
Proficient in SQL, pandas, and data visualization.
Led cross-functional teams to deliver analytics solutions.
"""

JD_KEYWORDS = {
    "must_have": ["Python", "machine learning", "SQL"],
    "nice_to_have": ["TensorFlow", "Spark"],
    "skills": ["pandas", "scikit-learn", "R", "Tableau"],
}


class TestComputeMatchScore(unittest.TestCase):

    def test_returns_required_keys(self):
        result = compute_match_score(RESUME, JD_KEYWORDS)
        for key in ("overall_score", "matched", "missing", "weak"):
            self.assertIn(key, result)

    def test_score_in_valid_range(self):
        result = compute_match_score(RESUME, JD_KEYWORDS)
        self.assertGreaterEqual(result["overall_score"], 0)
        self.assertLessEqual(result["overall_score"], 100)

    def test_known_keywords_matched(self):
        result = compute_match_score(RESUME, JD_KEYWORDS)
        matched_lower = [m.lower() for m in result["matched"]]
        self.assertIn("python", matched_lower)
        self.assertIn("sql", matched_lower)
        self.assertIn("scikit-learn", matched_lower)

    def test_missing_keyword_detected(self):
        result = compute_match_score(RESUME, JD_KEYWORDS)
        missing_lower = [m.lower() for m in result["missing"]]
        self.assertIn("spark", missing_lower)

    def test_empty_resume_scores_zero(self):
        result = compute_match_score("", JD_KEYWORDS)
        self.assertEqual(result["overall_score"], 0)
        self.assertEqual(result["matched"], [])

    def test_empty_keywords_returns_empty_lists(self):
        empty_kw = {"must_have": [], "nice_to_have": [], "skills": []}
        result = compute_match_score(RESUME, empty_kw)
        self.assertEqual(result["matched"], [])
        self.assertEqual(result["missing"], [])
        self.assertEqual(result["overall_score"], 0)

    def test_matched_and_missing_cover_all_keywords(self):
        result = compute_match_score(RESUME, JD_KEYWORDS)
        all_kw = (
            JD_KEYWORDS["must_have"]
            + JD_KEYWORDS["nice_to_have"]
            + JD_KEYWORDS["skills"]
        )
        combined = result["matched"] + result["missing"]
        self.assertEqual(sorted(combined), sorted(all_kw))


if __name__ == "__main__":
    unittest.main()
