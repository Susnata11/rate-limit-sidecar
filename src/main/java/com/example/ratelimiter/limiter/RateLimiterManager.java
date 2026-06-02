
package com.example.ratelimiter.limiter;
import io.github.bucket4j.*;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class RateLimiterManager {
  private final Map<String,Bucket> buckets=new ConcurrentHashMap<>();
  private Bucket createBucket(){
    return Bucket.builder().addLimit(
      Bandwidth.classic(5, Refill.intervally(3, Duration.ofSeconds(60)))
    ).build();
  }
  public boolean tryConsume(String key,long tokens){
    Bucket bucket=buckets.get(key);
    if(bucket==null){
      Bucket newBucket=createBucket();
      Bucket existingBucket=buckets.putIfAbsent(key,newBucket);
      bucket=existingBucket==null ? newBucket : existingBucket;
    }
    return bucket.tryConsume(tokens);
  }
}
