package com.redisson.cache;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@Slf4j
public class RedissonTestConfiguration {

  @Bean
  @Qualifier("redissonClientA")
  RedissonClient redissonClientA(@Value("${dynamic.redis.address}") String redisAddress) {
    Config config = new Config();
    config.useSingleServer().setAddress(redisAddress);
    return Redisson.create(config);
  }

  @Bean
  @Qualifier("redissonClientB")
  RedissonClient redissonClientB(@Value("${dynamic.redis.address}") String redisAddress) {
    Config config = new Config();
    config.useSingleServer().setAddress(redisAddress);
    return Redisson.create(config);
  }
}
