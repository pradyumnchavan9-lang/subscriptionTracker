package com.subtracker.SubTracker.ratelimiter;


import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> userBuckets = new ConcurrentHashMap<>();

    private Bucket newBucket(){
        Refill refill = Refill.intervally(10, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(10,refill);
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public Bucket getUserBucket(String email){
        return userBuckets.computeIfAbsent(email, k -> newBucket());
    }
}
