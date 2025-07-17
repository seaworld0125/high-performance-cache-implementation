package com.redisson.cache.flag;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.redisson.cache.flag.RedissonFeatureFlagAspectTestConfiguration.FeatureFlagTestClass.SomeDependency;
import com.redisson.cache.flag.annotation.FeatureFlag;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

@Slf4j
class RedissonFeatureFlagAspectTestConfiguration {

  static final String NAMESPACE = "test-app";

  @Bean
  @Qualifier("featureFlagManager")
  public FeatureFlagManager featureFlagManager(RedissonClient redissonClientA) {
    var cacheName = RedissonKeyUtils.featureFlagHashKey("test-app");
    var remoteCache = redissonClientA.<String, Boolean>getMapCache(cacheName);
    Cache<String, Boolean> localCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(10))
        .maximumSize(1000)
        .initialCapacity(100)
        .build();

    return new RedissonFeatureFlagLocalCacheManager(remoteCache, localCache);
  }

  @Bean
  public FeatureFlagAspect featureFlagAspect(FeatureFlagManager featureFlagManager) {
    return new FeatureFlagAspect(featureFlagManager);
  }

  @Bean
  FeatureFlagTestClass featureFlagTestClass(SomeDependency someDependency) {
    return new FeatureFlagTestClass(someDependency);
  }

  @Bean
  SomeDependency someDependency() {
    return new SomeDependency();
  }

  @Slf4j
  @RequiredArgsConstructor
  static class FeatureFlagTestClass {

    private final SomeDependency someDependency;

    @FeatureFlag(name = "pilot-feature-enabled")
    public void voidReturnEnabledMethod() {
      someDependency.call();
    }

    @FeatureFlag(name = "pilot-feature-disabled", value = false)
    public void voidReturnDisabledMethod() {
      someDependency.call();
    }

    @FeatureFlag(name = "pilot-feature-disabled", value = false)
    public Object objectReturnDisabledMethod() {
      someDependency.call();
      return null;
    }

    static class SomeDependency {

      void call() {
        log.info("SomeDependency called");
      }
    }
  }
}
