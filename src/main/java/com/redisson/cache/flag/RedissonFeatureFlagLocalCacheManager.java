package com.redisson.cache.flag;

import com.github.benmanes.caffeine.cache.Cache;
import jakarta.validation.constraints.NotEmpty;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.map.event.EntryCreatedListener;
import org.redisson.api.map.event.EntryExpiredListener;
import org.redisson.api.map.event.EntryRemovedListener;
import org.redisson.api.map.event.EntryUpdatedListener;

@Slf4j
public class RedissonFeatureFlagLocalCacheManager implements FeatureFlagManager {

    protected final RMapCache<String, Boolean> remoteCache; // source of truth
    private final Cache<String, Boolean> localCache;

    public RedissonFeatureFlagLocalCacheManager(RMapCache<String, Boolean> remoteCache, Cache<String, Boolean> localCache) {
        this.remoteCache = remoteCache;
        this.localCache = localCache;

        remoteCache.addListener((EntryCreatedListener<String, Boolean>) event -> {
            log.debug("[RemoteCache-Event] entry created. add local cache. key: {} value: {}", event.getKey(), event.getValue());
            localCache.put(event.getKey(), event.getValue());
        });

        remoteCache.addListener((EntryExpiredListener<String, Boolean>) event -> {
            log.debug("[RemoteCache-Event] entry expired. remove local cache. key: {}", event.getKey());
            localCache.invalidate(event.getKey());
        });

        remoteCache.addListener((EntryUpdatedListener<String, Boolean>) event -> {
            log.debug("[RemoteCache-Event] entry updated. update local cache. key: {} value: {} -> {}", event.getKey(), event.getOldValue(), event.getValue());
            localCache.put(event.getKey(), event.getValue());
        });

        remoteCache.addListener((EntryRemovedListener<String, Boolean>) (event) -> {
            log.debug("[RemoteCache-Event] entry removed. remove local cache. key: {}", event.getKey());
            localCache.invalidate(event.getKey());
        });
    }

    @Override
    public boolean isEnabled(@NotEmpty String name, boolean defaultValue) {
        String key = RedissonKeyUtils.featureFlagEnabledKey(name);
        Boolean value = localCache.getIfPresent(key);
        if (value != null) {
            log.debug("[LocalCache] cache hit. key: {}, value: {}", key, value);
            return value;
        }

        value = remoteCache.get(key);
        if (value != null) {
            log.debug("[RemoteCache] cache hit. key: {}, value: {}", key, value);
            localCache.put(key, value);
            return value;
        }

        log.debug("[LocalCache & RemoteCache] cache miss. key: {}", key);
        return defaultValue;
    }

    @Override
    public void setEnabled(@NotEmpty String name, boolean enabled, Duration duration) {
        String key = RedissonKeyUtils.featureFlagEnabledKey(name);
        log.debug("[LocalCache & RemoteCache] init. key: {}, value: {}, ttl: {}", key, enabled, duration);
        remoteCache.put(key, enabled, duration.toMillis(), TimeUnit.MILLISECONDS);
        localCache.put(key, enabled);
    }

    @Override
    public void setEnabled(@NotEmpty String name, boolean enabled) {
        String key = RedissonKeyUtils.featureFlagEnabledKey(name);
        log.debug("[LocalCache & RemoteCache] init. key: {}, value: {}", key, enabled);
        remoteCache.put(key, enabled);
        localCache.put(key, enabled);
    }

    @Override
    public void remove(@NotEmpty String name) {
        String key = RedissonKeyUtils.featureFlagEnabledKey(name);
        log.debug("[LocalCache & RemoteCache] remove. key: {}", key);
        remoteCache.remove(key);
        localCache.invalidate(key);
    }
}
