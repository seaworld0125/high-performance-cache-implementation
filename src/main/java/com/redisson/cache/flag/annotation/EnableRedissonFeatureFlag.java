package com.redisson.cache.flag.annotation;

import com.redisson.cache.flag.RedissonFeatureFlagConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * AutoConfiguration을 통해 활성화하면, Redisson 기반의 피처 플래그 기능이 활성화됩니다.
 * */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(RedissonFeatureFlagConfiguration.class)
public @interface EnableRedissonFeatureFlag {

}
