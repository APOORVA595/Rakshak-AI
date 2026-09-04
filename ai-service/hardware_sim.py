import requests
import time
import random

url = "http://localhost:8080/api/checkpoints/emergency"

# List of active wristbands on the mountain
active_tourists = ["pes1ug23cs095", "tourist_002", "tourist_003"]

print("Starting multi-tourist live tracking simulation...\n")

for i in range(1, 6):
    for t_id in active_tourists:
        # Generate slightly different coordinates and heart rates for each person
        lat = round(12.93352 + random.uniform(0.0001, 0.0020), 5)
        lon = round(77.53492 + random.uniform(0.0001, 0.0020), 5)
        bpm = random.randint(80, 160)

        payload = {
            "touristId": t_id,
            "latitude": str(lat),
            "longitude": str(lon),
            "heartRate": f"{bpm} bpm"
        }

        try:
            response = requests.post(url, json=payload)
            print(f"[{t_id}] Ping {i} Sent -> Java Response: {response.json()['status']}")
        except Exception as e:
            print(f"System Error for {t_id}: {e}")
            
    print("-" * 50)
    time.sleep(3)  # Wait 3 seconds before the next group network burst

print("\nSimulation complete.")