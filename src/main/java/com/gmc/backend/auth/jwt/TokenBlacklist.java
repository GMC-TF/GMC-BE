package com.gmc.backend.auth.jwt;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklist {

    private final Map<String, Date> blacklisted = new ConcurrentHashMap<>();

    public void add(String token, Date expiration) {
        blacklisted.put(token, expiration);
    }

    public boolean isBlacklisted(String token) {
        return blacklisted.containsKey(token);
    }

    @Scheduled(fixedRate = 60_000)
    public void removeExpired() {
        Date now = new Date();
        blacklisted.entrySet().removeIf(entry -> entry.getValue().before(now));
    }
}
