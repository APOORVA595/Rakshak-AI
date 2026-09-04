package com.rakshakai.core_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rakshakai.core_service.models.Tourist;

public interface TouristRepository extends JpaRepository<Tourist, String> {
}