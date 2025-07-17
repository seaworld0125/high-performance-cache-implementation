package com.redisson.cache.flag;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.event.EntryCreatedListener;
import org.redisson.api.map.event.EntryEvent;
import org.redisson.api.map.event.EntryExpiredListener;
import org.redisson.api.map.event.EntryRemovedListener;
import org.redisson.api.map.event.EntryUpdatedListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

@Slf4j
class RedissonFeatureFlagLocalCacheManagerEventTestConfiguration {

  @Bean
  @Qualifier("managerA")
  public TestRedissonFeatureFlagLocalCacheManager managerA(RedissonClient redissonClientA) {
    var cacheName = RedissonKeyUtils.featureFlagHashKey("test-app");
    var remoteCache = redissonClientA.<String, Boolean>getMapCache(cacheName);
    Cache<String, Boolean> localCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(10))
        .maximumSize(1000)
        .initialCapacity(100)
        .build();

    return new TestRedissonFeatureFlagLocalCacheManager(remoteCache, localCache);
  }

  @Bean
  @Qualifier("managerB")
  public TestRedissonFeatureFlagLocalCacheManager managerB(RedissonClient redissonClientB) {
    var cacheName = RedissonKeyUtils.featureFlagHashKey("test-app");
    var remoteCache = redissonClientB.<String, Boolean>getMapCache(cacheName);
    Cache<String, Boolean> localCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(10))
        .maximumSize(1000)
        .initialCapacity(100)
        .build();

    return new TestRedissonFeatureFlagLocalCacheManager(remoteCache, localCache);
  }

  static class TestRedissonFeatureFlagLocalCacheManager extends RedissonFeatureFlagLocalCacheManager {

    public TestRedissonFeatureFlagLocalCacheManager(RMapCache<String, Boolean> remoteCache, Cache<String, Boolean> localCache) {
      super(remoteCache, localCache);
    }

    void addRunnerOnEvent(Consumer<EntryEvent<String, Boolean>> consumer) {
      super.remoteCache.addListener((EntryCreatedListener<String, Boolean>) consumer::accept);
      super.remoteCache.addListener((EntryExpiredListener<String, Boolean>) consumer::accept);
      super.remoteCache.addListener((EntryUpdatedListener<String, Boolean>) consumer::accept);
      super.remoteCache.addListener((EntryRemovedListener<String, Boolean>) consumer::accept);
    }
  }
}
