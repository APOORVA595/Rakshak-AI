package com.rakshakai.core_service.controllers;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/checkpoints")
public class CheckpointController {

    @PostMapping("/scan")
    public Map<String, Object> rfidTap(@RequestBody Map<String, String> tapData) {
        String touristId = tapData.get("touristId");
        String location = tapData.get("location");

        // Simulate recording the physical card tap in the database
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ACCESS GRANTED");
        response.put("message", "Tourist " + touristId + " passed through " + location);
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
}