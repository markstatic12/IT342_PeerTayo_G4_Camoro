package edu.cit.camoro.peertayo.auth.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String token, long expiryMs) {
        blacklist.put(token, expiryMs);
        log.debug("Token blacklisted; blacklist size: {}", blacklist.size());
    }

    public boolean isBlacklisted(String token) {
        return blacklist.containsKey(token);
    }

    @Scheduled(fixedDelay = 3_600_000)
    public void evictExpiredTokens() {
        long now = System.currentTimeMillis();
        int[] removed = {0};
        blacklist.entrySet().removeIf(entry -> {
            if (entry.getValue() < now) {
                removed[0]++;
                return true;
            }
            return false;
        });
        if (removed[0] > 0) {
            log.info("Evicted {} expired tokens from blacklist", removed[0]);
        }
    }
}
