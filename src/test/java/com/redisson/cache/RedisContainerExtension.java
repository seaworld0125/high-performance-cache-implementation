package com.redisson.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slf4j
@Testcontainers
public abstract class RedisContainerExtension {

  private static final int REDIS_PORT = 6379;

  @Container
  // static으로 선언해야 모든 테스트 클래스에서 컨테이너를 재사용합니다.
  private static final GenericContainer<?> REDIS_CONTAINER =
      new GenericContainer<>("redis:7.4.1-alpine3.20")
          .withExposedPorts(REDIS_PORT)
          .withReuse(true);

  @DynamicPropertySource
  private static void registerRedisProperties(DynamicPropertyRegistry registry) {
    // 컨테이너가 시작된 후에 동적으로 주소를 가져와 Spring 프로퍼티로 등록합니다.
    registry.add("dynamic.redis.address",
        () -> String.format("redis://%s:%d",
            REDIS_CONTAINER.getHost(),
            REDIS_CONTAINER.getMappedPort(REDIS_PORT)
        )
    );
  }
}