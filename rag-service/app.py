from flask import Flask, request, jsonify
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import os
import requests

app = Flask(__name__)

CORE_SERVICE_URL = "http://core-service:8080"
SOP_DIR = "sops"
MIN_SCORE = 0.05

def load_sops():
    docs = []
    for name in sorted(os.listdir(SOP_DIR)):
        if name.endswith(".txt"):
            with open(os.path.join(SOP_DIR, name), encoding="utf-8-sig") as f:
                docs.append({"name": name[:-4], "text": f.read().strip()})
    return docs

SOPS = load_sops()
vectorizer = TfidfVectorizer(stop_words="english", ngram_range=(1, 2))
SOP_MATRIX = vectorizer.fit_transform([d["text"] for d in SOPS])

@app.route("/api/rag/suggest", methods=["POST"])
def suggest():
    data = request.get_json() or {}
    tourist_id = data.get("touristId")
    situation = data.get("situation", "")
    conditions = ""

    if tourist_id:
        try:
            resp = requests.get(f"{CORE_SERVICE_URL}/api/tourists/{tourist_id}", timeout=5)
            tourist = resp.json() or {}
            conditions = tourist.get("medicalConditions") or ""
        except Exception as e:
            return jsonify({"error": f"Could not reach core-service: {str(e)}"}), 500

    query = f"{situation} {conditions}".strip()
    if not query:
        return jsonify({"error": "Provide a situation and/or a touristId"}), 400

    scores = cosine_similarity(vectorizer.transform([query]), SOP_MATRIX)[0]
    ranked = sorted(zip(scores, SOPS), key=lambda x: -x[0])[:2]
    results = [
        {"protocol": d["name"], "score": round(float(s), 3), "steps": d["text"]}
        for s, d in ranked if s >= MIN_SCORE
    ]

    if not results:
        return jsonify({
            "result": "NO_RELEVANT_PROTOCOL",
            "message": "No stored protocol matches this situation. Follow standard first aid and contact medical help.",
            "query": query
        })

    return jsonify({"result": "PROTOCOLS_FOUND", "query": query, "protocols": results})

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001)
