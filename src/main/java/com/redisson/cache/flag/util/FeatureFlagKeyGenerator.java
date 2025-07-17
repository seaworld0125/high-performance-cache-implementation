package com.redisson.cache.flag.util;

public class FeatureFlagKeyGenerator {

  public static String featureFlagHashKey(String namespace) {
    return namespace + ":feature-flag";
  }

  public static String featureFlagEnabledKey(String name) {
    return name + ":enabled";
  }

  public static String featureFlagEnableFullKey(String namespace, String name) {
    return featureFlagHashKey(namespace) + ":" + featureFlagEnabledKey(name);
  }
}
