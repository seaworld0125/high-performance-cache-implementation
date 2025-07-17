package com.redisson.cache.flag;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.redis.feature-flag")
public record RedissonFeatureFlagProperties(
  Boolean enableLocalCache,
  RedissonFeatureFlagLocalCacheProperties localCache
) {

  public RedissonFeatureFlagProperties {
    if (enableLocalCache == null) {
      enableLocalCache = false; // 기본값: 로컬 캐시 비활성화
    }
  }

  public record RedissonFeatureFlagLocalCacheProperties(
    Duration ttl,
    Integer maxSize,
    Integer initialCapacity
  ) {

    public RedissonFeatureFlagLocalCacheProperties {
      if (ttl == null) {
        ttl = Duration.ofMinutes(1); // 기본값: 1분
      }
      if (maxSize == null) {
        maxSize = 10000; // 기본값: 10000개
      }
      if (initialCapacity == null) {
        initialCapacity = 1000; // 기본값: 1000개
      }
    }
  }
}
