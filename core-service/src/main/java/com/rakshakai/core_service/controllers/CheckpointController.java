package com.rakshakai.core_service.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rakshakai.core_service.models.Tourist;
import com.rakshakai.core_service.models.TrackingHistory;
import com.rakshakai.core_service.repositories.TouristRepository;
import com.rakshakai.core_service.repositories.TrackingHistoryRepository;

@RestController
@RequestMapping("/api/checkpoints")
public class CheckpointController {

    @Autowired
    private TouristRepository touristRepository;

    @Autowired
    private TrackingHistoryRepository trackingRepository;

    // METHOD A: Normal RFID Gate Taps (Last Known Location)
    @PostMapping("/scan")
    public Map<String, Object> rfidTap(@RequestBody Map<String, String> tapData) {
        String touristId = tapData.get("touristId");
        String location = tapData.get("location");

        Map<String, Object> response = new HashMap<>();
        response.put("status", "ACCESS GRANTED");
        response.put("message", "Tourist " + touristId + " passed through " + location);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    // METHOD B: Active LoRaWAN / GPS Pings (Emergency Tracking)
    @PostMapping("/emergency")
    public Map<String, Object> emergencyPing(@RequestBody Map<String, String> pingData) {
        String touristId = pingData.get("touristId");
        String lat = pingData.get("latitude");
        String lon = pingData.get("longitude");
        String heartRate = pingData.getOrDefault("heartRate", "UNKNOWN");

        Map<String, Object> response = new HashMap<>();

        // 1. Look up the tourist in the Postgres database
        Optional<Tourist> touristOpt = touristRepository.findById(touristId);

        if (touristOpt.isPresent()) {
            Tourist tourist = touristOpt.get();
            
            // 2. Automatically update their status to SOS_ACTIVE
            tourist.setStatus("SOS_ACTIVE");
            touristRepository.save(tourist);

            // 3. Save this specific GPS ping to the tracking history table
            TrackingHistory history = new TrackingHistory();
            history.setTourist(tourist);
            history.setLatitude(lat);
            history.setLongitude(lon);
            history.setHeartRate(heartRate);
            history.setPingType("LORA_SOS");
            trackingRepository.save(history);

            response.put("status", "EMERGENCY_SOS_SAVED");
            response.put("message", "CRITICAL: Location saved for " + tourist.getFullName() + ". Rescue alerted.");
        } else {
            response.put("status", "ERROR");
            response.put("message", "Unregistered wristband ID: " + touristId);
        }
        
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    // METHOD C: Base Camp Dashboard (Fetch live path for the map)
    @GetMapping("/history/{touristId}")
    public List<TrackingHistory> getTouristHistory(@PathVariable String touristId) {
        return trackingRepository.findByTouristTouristIdOrderByRecordedAtDesc(touristId);
    }
}