package com.redisson.cache.flag.exception;

public class FeatureFlagDisabledException extends RuntimeException {

  public FeatureFlagDisabledException(String errorMessage) {
    super(errorMessage);
  }
}
