package com.redisson.cache.flag;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

class RedissonFeatureFlagManagerValidationTest {

  @Test
  void testMethodValidation() {
    RedissonClient redissonClient = Mockito.mock(RedissonClient.class);
    String namespace = "test-namespace";

    var context = new StaticApplicationContext();
    context.registerBean(MethodValidationPostProcessor.class, MethodValidationPostProcessor::new);
    context.registerBean("bean", RedissonFeatureFlagManager.class, redissonClient, namespace);
    context.refresh();

    var postProcessor = context.getBean(FeatureFlagManager.class);
    Assertions.assertThrows(ConstraintViolationException.class, () -> postProcessor.isEnabled("", true));
    context.close();
  }
}