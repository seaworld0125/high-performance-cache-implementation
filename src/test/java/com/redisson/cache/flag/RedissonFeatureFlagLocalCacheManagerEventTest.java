package com.redisson.cache.flag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redisson.cache.RedisContainerExtension;
import com.redisson.cache.RedissonTestConfiguration;
import com.redisson.cache.flag.RedissonFeatureFlagLocalCacheManagerEventTestConfiguration.TestRedissonFeatureFlagLocalCacheManager;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.event.EntryEvent.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    AopAutoConfiguration.class,
    RedissonTestConfiguration.class,
    RedissonFeatureFlagLocalCacheManagerEventTestConfiguration.class,
})
public class RedissonFeatureFlagLocalCacheManagerEventTest extends RedisContainerExtension {

  @Autowired
  private TestRedissonFeatureFlagLocalCacheManager managerA;

  @Autowired
  private TestRedissonFeatureFlagLocalCacheManager managerB;

  @Autowired
  private RedissonClient redissonClientA;

  @Autowired
  private RedissonClient redissonClientB;

  final String FEATURE_FLAG_FIELD_NAME = "test-feature";

  @BeforeEach
  void setUp() {
    redissonClientA.getKeys().flushall();
    log.info("setUp------------------------------------");
  }

  @AfterEach
  void tearDown() {
    log.info("tearDown------------------------------------");
    redissonClientA.getKeys().flushall();
  }

  @Test
  @DisplayName("updated event test")
  void updateTest() throws InterruptedException {
    // given
    managerA.setEnabled(FEATURE_FLAG_FIELD_NAME, false);

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<Type> receivedEventType = new AtomicReference<>();
    managerA.addRunnerOnEvent(event -> {
      latch.countDown();
      receivedEventType.set(event.getType());
    });

    // when
    managerB.setEnabled(FEATURE_FLAG_FIELD_NAME, true);

    // then
    boolean eventReceived = latch.await(10, TimeUnit.SECONDS);
    assertTrue(eventReceived, "updated 이벤트가 수신되지 않았습니다.");
    assertEquals(Type.UPDATED, receivedEventType.get(), "수신된 이벤트 타입이 예상과 다릅니다.");
  }

  @Test
  @DisplayName("removed event test")
  void removeTest() throws InterruptedException {
    // given
    managerA.setEnabled(FEATURE_FLAG_FIELD_NAME, false);

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<Type> receivedEventType = new AtomicReference<>();
    managerA.addRunnerOnEvent(event -> {
      latch.countDown();
      receivedEventType.set(event.getType());
    });

    // when
    managerB.remove(FEATURE_FLAG_FIELD_NAME);

    // then
    boolean eventReceived = latch.await(10, TimeUnit.SECONDS);
    assertTrue(eventReceived, "removed 이벤트가 수신되지 않았습니다.");
    assertEquals(Type.REMOVED, receivedEventType.get(), "수신된 이벤트 타입이 예상과 다릅니다.");
  }

  @Test
  @DisplayName("expired event test")
  void expireTest() throws InterruptedException {
    // given
    managerB.setEnabled(FEATURE_FLAG_FIELD_NAME, false, Duration.ofSeconds(2));

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<Type> receivedEventType = new AtomicReference<>();
    managerA.addRunnerOnEvent(event -> {
      latch.countDown();
      receivedEventType.set(event.getType());
    });

    // then
    boolean eventReceived = latch.await(10, TimeUnit.SECONDS);
    assertTrue(eventReceived, "expired 이벤트가 수신되지 않았습니다.");
    assertEquals(Type.EXPIRED, receivedEventType.get(), "수신된 이벤트 타입이 예상과 다릅니다.");
  }

  @Test
  @DisplayName("created event test")
  void createTest() throws InterruptedException {
    // given
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<Type> receivedEventType = new AtomicReference<>();
    managerA.addRunnerOnEvent(event -> {
      latch.countDown();
      receivedEventType.set(event.getType());
    });

    // when
    managerB.setEnabled(FEATURE_FLAG_FIELD_NAME, false);

    // then
    boolean eventReceived = latch.await(10, TimeUnit.SECONDS);
    assertTrue(eventReceived, "created 이벤트가 수신되지 않았습니다.");
    assertEquals(Type.CREATED, receivedEventType.get(), "수신된 이벤트 타입이 예상과 다릅니다.");
  }

  @Test
  @DisplayName("set test")
  void setTest() {
    // given
    managerA.setEnabled(FEATURE_FLAG_FIELD_NAME, true);

    // when
    var rMap = redissonClientA.<String, Boolean>getMap(RedissonKeyUtils.featureFlagHashKey(RedissonFeatureFlagAspectTestConfiguration.NAMESPACE));

    // then
    boolean value = rMap.containsKey(RedissonKeyUtils.featureFlagEnabledKey(FEATURE_FLAG_FIELD_NAME));
    assertTrue(value, "set이 정상적으로 되지 않았습니다.");
  }
}
