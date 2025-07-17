package com.redisson.cache.flag.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드에 Redis 기반 피처 플래그(Feature Flag) 기능을 적용합니다.
 * <p>
 * 이 어노테이션이 적용된 메서드는 Redis에 저장된 플래그 상태에 따라 호출 여부가 결정됩니다.
 * 생성되는 Redis 키의 구조는 {@code <namespace>:feature-flag:<name>:enabled} 입니다.
 *
 * <li>name: 피처 플래그의 고유한 이름. Redis 키의 일부로 사용됩니다.</li>
 * <li>value: Redis에 해당 플래그 키가 존재하지 않을 경우 사용할 기본 상태 값. (기본: {@code true})</li>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FeatureFlag {

    String name();

    boolean value() default true;
}
