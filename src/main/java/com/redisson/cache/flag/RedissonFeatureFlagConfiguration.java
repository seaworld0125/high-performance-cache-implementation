package com.redisson.cache.flag;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.redisson.cache.flag.util.FeatureFlagKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Slf4j
@EnableConfigurationProperties(RedissonFeatureFlagProperties.class)
public class RedissonFeatureFlagConfiguration {
  @Value("${spring.application.name}")
  private String applicationName;

  /**
   * 빈 선언이 되지 않을 경우 오류 알림을 위해 설정
   */
  @Bean
  @ConditionalOnMissingBean(RedissonClient.class)
  public RedissonClient missingRedissonClientHandler() {
    throw new IllegalStateException("정의된 RedissonClient bean 이 없습니다. Redisson 기반 피처 플래그 사용시 RedissonClient bean 등록이 필요합니다.");
  }

  @Bean
  public FeatureFlagManager redissonFeatureFlagManager(
      RedissonClient redissonClient,
      RedissonFeatureFlagProperties redissonFeatureFlagProperties
  ) {
    if(redissonFeatureFlagProperties.enableLocalCache()) {
      var redissonFeatureFlagLocalCacheProperties = redissonFeatureFlagProperties.localCache();
      var cacheName = FeatureFlagKeyGenerator.featureFlagHashKey(applicationName);

      RMapCache<String, Boolean> remoteCache = redissonClient.getMapCache(cacheName);
      Cache<String, Boolean> localCache = Caffeine.newBuilder()
          .expireAfterWrite(redissonFeatureFlagLocalCacheProperties.ttl())
          .maximumSize(redissonFeatureFlagLocalCacheProperties.maxSize())
          .initialCapacity(redissonFeatureFlagLocalCacheProperties.initialCapacity())
          .build();

      return new RedissonFeatureFlagLocalCacheManager(remoteCache, localCache);
    }
    return new RedissonFeatureFlagManager(redissonClient, applicationName);
  }

  @Bean
  public FeatureFlagAspect redissonFeatureFlagAspect(FeatureFlagManager featureFlagManager) {
    return new FeatureFlagAspect(featureFlagManager);
  }
}
