package com.example.snipr.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * @Entity tells Hibernate (JPA's implementation) "this class maps to a
 * database table." One instance of this class = one row.
 *
 * We don't use Lombok here on purpose for now -- writing the getters/setters
 * by hand makes it obvious there's no magic: JPA needs a no-arg constructor
 * (it builds objects via reflection, then fills fields in) and it needs a
 * way to read/write each field, hence getters and setters.
 */
@Entity
@Table(name = "url_mapping")
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // let the DB auto-increment this
    private Long id;

    @Column(nullable = false, length = 2048)
    private String longUrl;

    @Column(unique = true, length = 16)
    private String shortCode;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    // JPA requires a no-arg constructor -- it's how Hibernate instantiates
    // objects before populating them from a DB row.
    public UrlMapping() {}

    public UrlMapping(String longUrl) {
        this.longUrl = longUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLongUrl() { return longUrl; }
    public void setLongUrl(String longUrl) { this.longUrl = longUrl; }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}