package com.rakshakai.core_service.controllers;

import java.util.HashMap;
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
import com.rakshakai.core_service.repositories.TouristRepository;

@RestController
@RequestMapping("/api/tourists")
public class TouristController {

    @Autowired
    private TouristRepository TouristRepository;

    @PostMapping("/register")
    public Tourist registerTourist(@RequestBody Tourist tourist) {
        return TouristRepository.save(tourist); 
    }

    // Fetch one tourist's full profile, including their stored face vector
    @GetMapping("/{touristId}")
    public Tourist getTourist(@PathVariable String touristId) {
        return TouristRepository.findById(touristId).orElse(null);
    }

    // Safely update JUST the face vector, without touching any other field
    @PostMapping("/{touristId}/face-vector")
    public Map<String, Object> updateFaceVector(@PathVariable String touristId, @RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        Optional<Tourist> touristOpt = TouristRepository.findById(touristId);

        if (touristOpt.isPresent()) {
            Tourist tourist = touristOpt.get();
            tourist.setFaceVector(body.get("faceVector"));
            TouristRepository.save(tourist);
            response.put("status", "SUCCESS");
        } else {
            response.put("status", "ERROR");
            response.put("message", "Unregistered tourist: " + touristId);
        }
        return response;
    }
}