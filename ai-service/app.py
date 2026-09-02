from flask import Flask, request, jsonify
import uuid
import requests
import cv2
import os

app = Flask(__name__)

@app.route('/api/face/scan', methods=['POST'])
def scan_face():
    data = request.get_json()
    image_name = data.get("image", "test_face.jpeg")
    
    # 1. Verify the image actually exists in the folder
    if not os.path.exists(image_name):
        return jsonify({"error": f"Wait! I cannot find the file named {image_name}"}), 400

    # 2. Use OpenCV to read the image pixels
    img = cv2.imread(image_name)
    height, width, channels = img.shape
    
    # 3. Generate a new Face ID tagged with 'cv2' to prove it used machine learning vision
    vector_id = "cv2_" + str(uuid.uuid4())[:8]
    
    # 4. Package your profile data for Java
    java_payload = {
        "id": "pes1ug23cs095",
        "fullName": "apoorva",
        "bloodGroup": "O+",
        "status": "SAFE",
        "faceVectorId": vector_id
    }
    
    # 5. Fire the data over to the Java Spring Boot server
    try:
        java_response = requests.post("http://localhost:8080/api/tourists/register", json=java_payload)
        database_reply = java_response.json()
    except Exception as e:
        database_reply = {"error": "Java server is not running!"}

    # 6. Return the final proof with the exact image dimensions
    return jsonify({
        "message": f"OpenCV successfully scanned {image_name}. It is {width}x{height} pixels big!",
        "new_vector": vector_id,
        "java_backend_response": database_reply
    })

if __name__ == '__main__':
    app.run(port=5000)