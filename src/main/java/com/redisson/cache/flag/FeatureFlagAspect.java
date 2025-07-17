package com.redisson.cache.flag;

import com.redisson.cache.flag.annotation.FeatureFlag;
import com.redisson.cache.flag.exception.FeatureFlagDisabledException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

@Slf4j
@RequiredArgsConstructor
@Aspect
public class FeatureFlagAspect {

  private final FeatureFlagManager featureFlagManager;

  @Pointcut("@annotation(com.redisson.cache.flag.annotation.FeatureFlag)")
  public void onInvoke() {
    // nothing
  }

  @Around("com.redisson.cache.flag.FeatureFlagAspect.onInvoke()")
  public Object tryIfFeatureFlagEnabled(ProceedingJoinPoint pjp) throws Throwable {
    var signature = (MethodSignature) pjp.getSignature();
    var method = signature.getMethod();

    FeatureFlag featureFlag = method.getAnnotation(FeatureFlag.class);
    final boolean isFeatureFlagEnabled = featureFlagManager.isEnabled(featureFlag.name(), featureFlag.value());

    log.debug("featureFlag name: {} | isEnabled: {}", featureFlag.name(), isFeatureFlagEnabled);
    if (isFeatureFlagEnabled) {
      return pjp.proceed(pjp.getArgs());
    } else if (method.getReturnType().equals(Void.TYPE)) {
      return null;
    } else {
      throw new FeatureFlagDisabledException(method.getName() + " feature-flag disabled: shutdown");
    }
  }
}
