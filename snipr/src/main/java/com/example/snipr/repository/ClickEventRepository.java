package com.example.snipr.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.snipr.model.ClickEvent;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {
    // We'll use this in the analytics dashboard step later --
    // Spring generates: SELECT * FROM click_event WHERE short_code = ?
    List<ClickEvent> findByShortCode(String shortCode);
}