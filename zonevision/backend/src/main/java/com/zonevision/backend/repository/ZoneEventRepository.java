package com.zonevision.backend.repository;

import com.zonevision.backend.model.ZoneEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZoneEventRepository extends JpaRepository<ZoneEvent, Long> {

    List<ZoneEvent> findTop50ByOrderByOccurredAtDesc();

    List<ZoneEvent> findByZoneNameOrderByOccurredAtDesc(String zoneName);
}
