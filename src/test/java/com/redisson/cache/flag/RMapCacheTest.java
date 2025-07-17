package com.redisson.cache.flag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redisson.cache.RedisContainerExtension;
import com.redisson.cache.RedissonTestConfiguration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.event.EntryCreatedListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    AopAutoConfiguration.class,
    RedissonTestConfiguration.class,
})
public class RMapCacheTest extends RedisContainerExtension {

  @Autowired
  private RedissonClient redissonClientA;

  @Autowired
  private RedissonClient redissonClientB;

  @BeforeEach
  void setUp() {
    redissonClientA.getKeys().flushall();
    log.info("setUp------------------------------------");
  }

  @Test
  void testCreatedEventAcrossClients() throws InterruptedException {
    // given
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<String> receivedKeyEvent = new AtomicReference<>();

    // when
    // [클라이언트 A]
    RMapCache<String, String> mapFromClientA = redissonClientA.getMapCache("test-app");
    int listenerId = mapFromClientA.addListener((EntryCreatedListener<String, String>) event -> {
      System.out.println("이벤트 수신! Key: " + event.getKey());
      receivedKeyEvent.set(event.getKey()); // 받은 이벤트의 키 저장
      latch.countDown(); // Latch 카운트 감소 (대기 종료 신호)
    });

    // [클라이언트 B] EntryCreated 이벤트 발행
    RMapCache<String, String> mapFromClientB = redissonClientB.getMapCache("test-app");
    mapFromClientB.put("myNewKey", "myValue", 1, TimeUnit.MINUTES);

    // then
    boolean eventReceived = latch.await(5, TimeUnit.SECONDS);
    mapFromClientA.removeListener(listenerId);

    // 3. 검증
    assertTrue(eventReceived, "5초 내에 이벤트가 수신되지 않았습니다.");
    assertEquals("myNewKey", receivedKeyEvent.get(), "이벤트로 전달된 키가 일치하지 않습니다.");
  }
}
