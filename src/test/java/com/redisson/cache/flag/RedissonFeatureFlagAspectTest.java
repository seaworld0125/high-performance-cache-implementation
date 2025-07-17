package com.redisson.cache.flag;

import com.redisson.cache.RedisContainerExtension;
import com.redisson.cache.RedissonTestConfiguration;
import com.redisson.cache.flag.RedissonFeatureFlagAspectTestConfiguration.FeatureFlagTestClass;
import com.redisson.cache.flag.RedissonFeatureFlagAspectTestConfiguration.FeatureFlagTestClass.SomeDependency;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@ExtendWith({SpringExtension.class})
@ContextConfiguration(classes = {
    AopAutoConfiguration.class,
    RedissonTestConfiguration.class,
    RedissonFeatureFlagAspectTestConfiguration.class,
})
public class RedissonFeatureFlagAspectTest extends RedisContainerExtension {

  @Autowired
  private FeatureFlagTestClass featureFlagTestClass;

  @MockitoSpyBean
  private SomeDependency someDependency;

  @DisplayName("피처 플래그가 활성화된 경우 메소드가 실행되어야 한다")
  @Test
  void test_1() {
    featureFlagTestClass.voidReturnEnabledMethod();

    Mockito.verify(someDependency, Mockito.times(1))
        .call();
  }


  @DisplayName("피처 플래그가 비활성화된 경우 메소드가 실행되지 않아야 한다")
  @Test
  void test_2() {
    featureFlagTestClass.voidReturnDisabledMethod();

    Mockito.verify(someDependency, Mockito.never())
        .call();
  }

  @DisplayName("피처 플래그가 비활성화된 경우 반환값이 있으면 예외가 발생해야 한다.")
  @Test
  void test_3() {
    Assertions.assertThrows(RuntimeException.class, () -> featureFlagTestClass.objectReturnDisabledMethod());

    Mockito.verify(someDependency, Mockito.never())
        .call();
  }
}
