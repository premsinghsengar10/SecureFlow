from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Dict, Optional
import random

app = FastAPI()

class EmailFeatures(BaseModel):
    subject_len: int
    num_links: int
    has_attachment: bool
    has_urgent_words: bool
    sender_in_contacts: bool
    spf_result: Optional[str] = None
    dkim_result: Optional[str] = None
    body_text: Optional[str] = ""
    subject_text: Optional[str] = ""

class PredictionResponse(BaseModel):
    scores: Dict[str, float]
    explanations: List[str]

@app.post("/predict", response_model=PredictionResponse)
def predict(features: EmailFeatures):
    # MVP Logic: Hybrid Rule-based + Dummy ML Score
    
    scores = {
        "important": 0.1,
        "spam": 0.1,
        "fraud": 0.0,
        "other": 0.8
    }
    explanations = []

    # Rule 1: Sender in contacts -> High importance
    if features.sender_in_contacts:
        scores["important"] += 0.6
        explanations.append("Sender is in contacts")

    # Rule 2: Urgent words -> Potential fraud or important (context dependent)
    if features.has_urgent_words:
        scores["important"] += 0.2
        scores["fraud"] += 0.1
        explanations.append("Contains urgent language")

    # Rule 3: Many links -> Potential spam/promotion
    if features.num_links > 3:
        scores["spam"] += 0.4
        scores["important"] -= 0.1
        explanations.append("Contains many links")

    # Rule 4: Keywords in Subject (Meeting, Schedule, etc.)
    subject_lower = features.subject_text.lower() if features.subject_text else ""
    if "meeting" in subject_lower or "schedule" in subject_lower or "update" in subject_lower:
        scores["important"] += 0.7
        scores["other"] -= 0.5
        explanations.append("Subject contains important keywords")

    # Normalize scores (simple normalization)
    total = sum(scores.values())
    if total > 0:
        scores = {k: v / total for k, v in scores.items()}

    return {
        "scores": scores,
        "explanations": explanations
    }

@app.get("/health")
def health():
    return {"status": "ok"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
