package com.redisson.cache.flag;

import jakarta.validation.constraints.NotEmpty;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.validation.annotation.Validated;

@Slf4j
@RequiredArgsConstructor
@Validated
public class RedissonFeatureFlagManager implements FeatureFlagManager {

  protected final RedissonClient redissonClient;
  protected final String namespace;

  protected RBucket<Boolean> getBucket(@NotEmpty String name) {
    String key = RedissonKeyUtils.featureFlagEnableFullKey(namespace, name);
    return redissonClient.getBucket(key);
  }

  @Override
  public boolean isEnabled(
      @NotEmpty String name,
      boolean defaultValue
  ) {
    var bucket = getBucket(name);
    if (bucket.isExists()) {
      var value = bucket.get();
      log.debug("[RemoteCache] cache hit. name: {} value: {}", name, value);
      return value;
    } else {
      log.debug("[RemoteCache] cache miss. name: {}", name);
      return defaultValue;
    }
  }

  @Override
  public void setEnabled(
      @NotEmpty String name,
      boolean enabled,
      Duration duration
  ) {
    var bucket = getBucket(name);
    bucket.set(enabled, duration);
  }

  @Override
  public void setEnabled(
      @NotEmpty String name,
      boolean enabled
  ) {
    var bucket = getBucket(name);
    bucket.set(enabled);
  }

  @Override
  public void remove(@NotEmpty String name) {
    var bucket = getBucket(name);
    bucket.delete();
  }
}
