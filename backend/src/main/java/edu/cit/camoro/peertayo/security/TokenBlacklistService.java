package edu.cit.camoro.peertayo.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory token blacklist.
 *
 * When a user calls POST /api/v1/auth/logout the token is stored here
 * until its natural expiry, after which it is automatically evicted.
 *
 * The {@link JwtAuthenticationFilter} checks this list before accepting
 * any bearer token, so a logged-out token is rejected immediately.
 */
@Service
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

    /** token → expiry time in epoch-milliseconds */
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    /**
     * Add a token to the blacklist.
     *
     * @param token    the raw JWT string
     * @param expiryMs epoch-ms when the token naturally expires
     */
    public void blacklist(String token, long expiryMs) {
        blacklist.put(token, expiryMs);
        log.debug("Token blacklisted; blacklist size: {}", blacklist.size());
    }

    /** Returns {@code true} if the token has been explicitly invalidated. */
    public boolean isBlacklisted(String token) {
        return blacklist.containsKey(token);
    }

    /**
     * Runs every hour and purges entries whose JWTs have already expired
     * naturally – they are no longer a risk and just take up memory.
     */
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
