import threading
import time
import random
import os
import requests
import numpy as np
from sklearn.ensemble import IsolationForest

# Reads BACKEND_HOST from the environment if set (e.g. "core-service" inside Docker),
# otherwise falls back to "localhost" so this still runs exactly as before outside Docker.
BACKEND_HOST = os.environ.get("BACKEND_HOST", "localhost")
API_URL = f"http://{BACKEND_HOST}:8080/api/checkpoints/emergency"
TOURISTS_API = f"http://{BACKEND_HOST}:8080/api/checkpoints/tourists"

def simulate_tourist(tourist_id, start_lat, start_lon):
    # Initialize a local Isolation Forest model for this device
    model = IsolationForest(contamination=0.05, random_state=42)
    model.fit(np.array([[random.uniform(60, 100), random.uniform(0.5, 2.0)] for _ in range(100)]))
    
    lat, lon = start_lat, start_lon
    consecutive_anomalies = 0
    
    print(f"[Device Connected] Started simulation thread for: {tourist_id}")

    while True:
        # 15% random chance of entering distress
        is_emergency = random.random() < 0.15

        if not is_emergency:
            lat += random.uniform(-0.0001, 0.0001)
            lon += random.uniform(-0.0001, 0.0001)
            hr = random.randint(70, 95)
            speed = random.uniform(1.0, 1.5)
        else:
            hr = random.randint(155, 180)
            speed = 0.0

        # --- BULLETPROOF HYBRID SAFETY NET ---
        # Hard deterministic medical limits instantly catch extreme danger,
        # while the Isolation Forest model handles nuanced behavioral anomalies.
        if hr >= 170 or speed == 0.0:
            is_raw_anomaly = True
        else:
            prediction = model.predict([[hr, speed]])[0]
            is_raw_anomaly = bool(prediction == -1)

        raw_score = model.decision_function([[hr, speed]])[0]
        risk_score = round(max(0, -raw_score * 10), 2)
        
        if is_raw_anomaly:
            consecutive_anomalies += 1
        else:
            consecutive_anomalies = 0
            
        confirmed_sos = bool(consecutive_anomalies >= 2)

        payload = {
            "touristId": tourist_id,
            "latitude": str(round(lat, 6)),
            "longitude": str(round(lon, 6)),
            "heartRate": str(hr),
            "aiRiskScore": str(risk_score),
            "isAnomaly": str(confirmed_sos)
        }

        try:
            resp = requests.post(API_URL, json=payload, timeout=3)
            res_data = resp.json()
            
            # Listen for offline global evacuation downlink from Base Camp
            if res_data.get("evacuate") == True:
                print(f"🚨 [{tourist_id}] DOWNLINK: Evacuation order received on device!")
        except Exception:
            pass
            
        time.sleep(1.5)

if __name__ == "__main__":
    print("Fetching registered personnel from Base Camp backend...")
    try:
        response = requests.get(TOURISTS_API)
        tourists = response.json()
        
        if not tourists:
            print("❌ No tourists found in database. Register some users first!")
        else:
            print(f"Found {len(tourists)} registered profiles. Spawning threads...")
            
            # Base coordinates to scatter simulated devices slightly around the map
            base_lat, base_lon = 12.9340, 77.5350
            
            for index, t in enumerate(tourists):
                t_id = t['touristId']
                # Give each tourist a slightly offset starting point on the map
                lat_offset = base_lat + (index * 0.0008)
                lon_offset = base_lon + (index * 0.0008)
                
                # Spin up a lightweight daemon thread for each tourist
                t_thread = threading.Thread(target=simulate_tourist, args=(t_id, lat_offset, lon_offset), daemon=True)
                t_thread.start()
                
            print("\n🚀 All hardware threads running concurrently with hybrid safety overrides. Press Ctrl+C to stop.\n")
            while True:
                time.sleep(1)
                
    except requests.exceptions.ConnectionError:
        print("❌ Error: Could not connect to Spring Boot backend. Make sure it is running on port 8080.")