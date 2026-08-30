package com.example.snipr.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * One row per click on a short link. Append-only -- we never update or
 * delete these, only insert. This is deliberate: raw event data is the
 * "source of truth" for analytics. A running counter (like "clickCount"
 * on UrlMapping) can always be derived from these rows later (COUNT(*)
 * WHERE short_code = ?), but if we only kept a counter, we could never
 * reconstruct "how many clicks came from Twitter last Tuesday."
 */
@Entity
@Table(name = "click_event")
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 16)
    private String shortCode;

    @Column(nullable = false)
    private Instant clickedAt = Instant.now();

    @Column(length = 512)
    private String referrer; // which site sent the visitor here (e.g. twitter.com), null if direct

    @Column(length = 512)
    private String userAgent; // raw browser/device string, e.g. "Mozilla/5.0 ..."

    @Column(length = 64)
    private String ipAddress;

    public ClickEvent() {}

    public ClickEvent(String shortCode, String referrer, String userAgent, String ipAddress) {
        this.shortCode = shortCode;
        this.referrer = referrer;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
    }

    public Long getId() { return id; }
    public String getShortCode() { return shortCode; }
    public Instant getClickedAt() { return clickedAt; }
    public String getReferrer() { return referrer; }
    public String getUserAgent() { return userAgent; }
    public String getIpAddress() { return ipAddress; }
}