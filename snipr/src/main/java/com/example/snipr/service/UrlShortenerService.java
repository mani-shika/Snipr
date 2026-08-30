package com.example.snipr.service;

import com.example.snipr.model.ClickEvent;
import com.example.snipr.model.UrlMapping;
import com.example.snipr.repository.ClickEventRepository;
import com.example.snipr.repository.UrlRepository;
import com.example.snipr.util.Base62Encoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class UrlShortenerService {

    private final UrlRepository urlRepository;
    private final ClickEventRepository clickEventRepository;
    private final RedisTemplate<String, String> redisTemplate; // NEW

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.cache-ttl-seconds}")
    private long cacheTtlSeconds; // NEW

    public UrlShortenerService(UrlRepository urlRepository,
                                ClickEventRepository clickEventRepository,
                                RedisTemplate<String, String> redisTemplate) {
        this.urlRepository = urlRepository;
        this.clickEventRepository = clickEventRepository;
        this.redisTemplate = redisTemplate;
    }

    /**
     * UPDATED: now also writes into Redis right after creation.
     * Why pre-warm the cache here instead of waiting for the first visit?
     * Because a freshly created link is very likely to be clicked
     * immediately (someone just shared it) -- no reason to force that
     * very first visit to eat a "cache miss" penalty when we already
     * have the data sitting right here.
     */
    public UrlMapping createShortUrl(String longUrl) {
        UrlMapping mapping = new UrlMapping(longUrl);
        mapping = urlRepository.save(mapping);

        String shortCode = Base62Encoder.encode(mapping.getId());
        mapping.setShortCode(shortCode);
        mapping = urlRepository.save(mapping);

        redisTemplate.opsForValue().set(shortCode, longUrl, Duration.ofSeconds(cacheTtlSeconds));

        return mapping;
    }

    public String getFullShortUrl(UrlMapping mapping) {
        return baseUrl + mapping.getShortCode();
    }

    /**
     * REWRITTEN: this is the actual cache-aside pattern.
     *
     * 1. Ask Redis first: "do you have this short code?"
     *    - If YES (cache hit): return immediately. Database never touched.
     *      This is the fast path, and after Step 4, it'll be the path taken
     *      almost every time for any link that's been visited recently.
     *    - If NO (cache miss): fall through to step 2.
     * 2. Query the database (the "source of truth" -- Redis can be wiped,
     *    restarted, or evicted, but the DB row always exists).
     * 3. If found in the DB, WRITE it into Redis before returning, so the
     *    *next* request for this same code becomes a cache hit instead of
     *    repeating this same miss-then-DB-query cycle forever.
     *
     * This is why it's called "cache-aside": the cache sits *beside* the
     * database, and our own application code is responsible for keeping
     * it in sync -- Redis doesn't automatically know when the DB changes.
     */
    public Optional<String> resolveLongUrl(String shortCode) {
        String cached = redisTemplate.opsForValue().get(shortCode);
        if (cached != null) {
            return Optional.of(cached); // CACHE HIT -- no DB query at all
        }

        // CACHE MISS -- fall back to the database
        Optional<UrlMapping> fromDb = urlRepository.findByShortCode(shortCode);

        fromDb.ifPresent(mapping ->
                redisTemplate.opsForValue().set(
                        shortCode, mapping.getLongUrl(), Duration.ofSeconds(cacheTtlSeconds))
        );

        return fromDb.map(UrlMapping::getLongUrl);
    }

    public void recordClick(String shortCode, String referrer, String userAgent, String ipAddress) {
        ClickEvent event = new ClickEvent(shortCode, referrer, userAgent, ipAddress);
        clickEventRepository.save(event);
    }
}