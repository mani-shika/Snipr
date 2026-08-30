package com.example.snipr.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.snipr.model.UrlMapping;


public interface UrlRepository extends JpaRepository<UrlMapping, Long> {
    Optional<UrlMapping> findByShortCode(String shortCode);
}