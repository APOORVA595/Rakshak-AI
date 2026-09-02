package com.rakshakai.core_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
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
}