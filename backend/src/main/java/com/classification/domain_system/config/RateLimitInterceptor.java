package com.classification.domain_system.config;

import com.classification.domain_system.context.AuthContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final AuthContext authContext;
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    
    // Limits (defaults to 300 requests per minute to support burst UI smoke tests and hard navigations)
    private final int refillRate;
    private final int maxCapacity;

    @org.springframework.beans.factory.annotation.Autowired
    public RateLimitInterceptor(AuthContext authContext,
                                @org.springframework.beans.factory.annotation.Value("${rate-limit.capacity:300}") int maxCapacity,
                                @org.springframework.beans.factory.annotation.Value("${rate-limit.refill-rate:300}") int refillRate) {
        this.authContext = authContext;
        this.maxCapacity = maxCapacity;
        this.refillRate = refillRate;
    }

    public RateLimitInterceptor(AuthContext authContext) {
        this(authContext, 300, 300);
    }

    public RateLimitInterceptor() {
        this(null, 300, 300);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);
        String userId = authContext != null && authContext.getUserId() != null ? authContext.getUserId() : "anonymous";
        
        String key = userId.equals("anonymous") ? clientIp : userId;
        
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(maxCapacity, refillRate));
        
        if (bucket.tryConsume()) {
            return true;
        } else {
            log.warn("Rate limit exceeded for key: {}", key);
            response.setStatus(429);
            response.setHeader("Retry-After", "1");
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too Many Requests\", \"message\": \"Rate limit exceeded. Please try again later.\"}");
            return false;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    private static class TokenBucket {
        private final int maxCapacity;
        private final int refillRatePerMinute;
        private AtomicInteger tokens;
        private long lastRefillTime;

        public TokenBucket(int maxCapacity, int refillRatePerMinute) {
            this.maxCapacity = maxCapacity;
            this.refillRatePerMinute = refillRatePerMinute;
            this.tokens = new AtomicInteger(maxCapacity);
            this.lastRefillTime = Instant.now().toEpochMilli();
        }

        public synchronized boolean tryConsume() {
            refill();
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }

        private void refill() {
            long now = Instant.now().toEpochMilli();
            long timePassed = now - lastRefillTime;
            
            // Refill per minute logic
            int tokensToAdd = (int) (timePassed * refillRatePerMinute / 60000);
            if (tokensToAdd > 0) {
                tokens.set(Math.min(maxCapacity, tokens.get() + tokensToAdd));
                lastRefillTime = now;
            }
        }
    }
}
