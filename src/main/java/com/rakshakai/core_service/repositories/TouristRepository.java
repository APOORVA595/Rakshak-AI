package com.rakshakai.core_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rakshakai.core_service.models.Tourist;

@Repository
public interface TouristRepository extends JpaRepository<Tourist, String> {
}