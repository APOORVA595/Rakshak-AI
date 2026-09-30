from flask import Flask, request, jsonify
import face_recognition
import numpy as np
import requests
import base64
import json

app = Flask(__name__)

CORE_SERVICE_URL = "http://core-service:8080"

# Stricter threshold for 1-to-many search than 1-to-1 verify,
# since comparing against many people raises the chance of a random false match.
IDENTIFY_THRESHOLD = 0.55
# If the best and second-best matches are this close to each other,
# treat it as too ambiguous to confidently name one person.
AMBIGUITY_MARGIN = 0.05

def decode_image(image_b64):
    image_bytes = base64.b64decode(image_b64)
    temp_path = "/tmp/temp_face.jpg"
    with open(temp_path, "wb") as f:
        f.write(image_bytes)
    return temp_path

def get_face_encoding_from_b64(image_b64):
    image_path = decode_image(image_b64)
    image = face_recognition.load_image_file(image_path)
    encodings = face_recognition.face_encodings(image)
    if len(encodings) == 0:
        return None
    return encodings[0]

@app.route("/api/face/register", methods=["POST"])
def register_face():
    data = request.get_json()
    tourist_id = data.get("touristId")
    image_b64 = data.get("image")

    if not tourist_id or not image_b64:
        return jsonify({"error": "touristId and image are required"}), 400

    new_vector = get_face_encoding_from_b64(image_b64)
    if new_vector is None:
        return jsonify({"error": "No face detected in the image"}), 400

    face_vector_json = json.dumps(new_vector.tolist())

    try:
        resp = requests.post(
            f"{CORE_SERVICE_URL}/api/tourists/{tourist_id}/face-vector",
            json={"faceVector": face_vector_json}
        )
        java_reply = resp.json()
    except Exception as e:
        return jsonify({"error": f"Could not reach core-service: {str(e)}"}), 500

    return jsonify({
        "message": f"Face registered for {tourist_id}",
        "java_response": java_reply
    })

@app.route("/api/face/verify", methods=["POST"])
def verify_face():
    data = request.get_json()
    tourist_id = data.get("touristId")
    image_b64 = data.get("image")

    if not tourist_id or not image_b64:
        return jsonify({"error": "touristId and image are required"}), 400

    try:
        resp = requests.get(f"{CORE_SERVICE_URL}/api/tourists/{tourist_id}")
        tourist = resp.json()
    except Exception as e:
        return jsonify({"error": f"Could not reach core-service: {str(e)}"}), 500

    stored_vector_json = tourist.get("faceVector") if tourist else None
    if not stored_vector_json:
        return jsonify({"error": f"No face registered yet for {tourist_id}"}), 400

    stored_vector = np.array(json.loads(stored_vector_json))

    new_vector = get_face_encoding_from_b64(image_b64)
    if new_vector is None:
        return jsonify({"error": "No face detected in the new image"}), 400

    distance = np.linalg.norm(stored_vector - new_vector)
    is_match = bool(distance < 0.6)

    return jsonify({
        "touristId": tourist_id,
        "match": is_match,
        "distance": round(float(distance), 4),
        "confidence": round(max(0, (1 - distance)) * 100, 1)
    })

# --- Identify an unknown person by searching ALL registered tourists ---
@app.route("/api/face/identify", methods=["POST"])
def identify_face():
    data = request.get_json()
    image_b64 = data.get("image")

    if not image_b64:
        return jsonify({"error": "image is required"}), 400

    new_vector = get_face_encoding_from_b64(image_b64)
    if new_vector is None:
        return jsonify({"error": "No face detected in the image"}), 400

    try:
        resp = requests.get(f"{CORE_SERVICE_URL}/api/checkpoints/tourists")
        all_tourists = resp.json()
    except Exception as e:
        return jsonify({"error": f"Could not reach core-service: {str(e)}"}), 500

    # Compare against every tourist who actually has a registered face
    candidates = []
    for tourist in all_tourists:
        stored_vector_json = tourist.get("faceVector")
        if not stored_vector_json:
            continue
        stored_vector = np.array(json.loads(stored_vector_json))
        distance = float(np.linalg.norm(stored_vector - new_vector))
        candidates.append((distance, tourist))

    if len(candidates) == 0:
        return jsonify({"result": "NO_REGISTERED_FACES", "message": "No tourists have a registered face yet"}), 200

    # Sort so the closest (most likely) match comes first
    candidates.sort(key=lambda x: x[0])
    best_distance, best_tourist = candidates[0]

    # Case 1: even the best match isn't close enough to trust
    if best_distance > IDENTIFY_THRESHOLD:
        return jsonify({
            "result": "NO_MATCH_FOUND",
            "message": "This person does not match any registered tourist confidently.",
            "closest_distance": round(best_distance, 4)
        }), 200

    # Case 2: check if a second candidate is suspiciously close too
    if len(candidates) > 1:
        second_distance, second_tourist = candidates[1]
        if (second_distance - best_distance) < AMBIGUITY_MARGIN:
            return jsonify({
                "result": "AMBIGUOUS",
                "message": "Two or more registered tourists are too similar to confidently distinguish. Manual verification (e.g. wristband, ID) is required.",
                "candidates": [
                    {"touristId": best_tourist.get("touristId"), "fullName": best_tourist.get("fullName"), "distance": round(best_distance, 4)},
                    {"touristId": second_tourist.get("touristId"), "fullName": second_tourist.get("fullName"), "distance": round(second_distance, 4)}
                ]
            }), 200

    # Case 3: confident, unambiguous match
    medical_conditions = best_tourist.get("medicalConditions") or "None reported"
    has_medical_alert = medical_conditions.strip().lower() not in ["none", "none reported", ""]

    return jsonify({
        "result": "MATCH_FOUND",
        "touristId": best_tourist.get("touristId"),
        "fullName": best_tourist.get("fullName"),
        "bloodType": best_tourist.get("bloodType"),
        "emergencyContact": best_tourist.get("emergencyContact"),
        "medicalConditions": medical_conditions,
        "medicalAlert": has_medical_alert,
        "status": best_tourist.get("status"),
        "lastKnownLocation": best_tourist.get("lastKnownLocation"),
        "confidence": round(max(0, (1 - best_distance)) * 100, 1),
        "distance": round(best_distance, 4)
    })

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)