package com.rakshakai.core_service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rakshakai.core_service.models.TrackingHistory;

public interface TrackingHistoryRepository extends JpaRepository<TrackingHistory, Long> {
    // Custom method to fetch a tourist's path, sorted from newest to oldest
    List<TrackingHistory> findByTouristTouristIdOrderByRecordedAtDesc(String touristId);
}