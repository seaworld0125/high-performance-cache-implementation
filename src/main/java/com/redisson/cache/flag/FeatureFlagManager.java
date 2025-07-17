package com.redisson.cache.flag;

import jakarta.validation.constraints.NotEmpty;
import java.time.Duration;

/**
 * Redis를 기반으로 동작하는 피처 플래그(Feature Flag)의 상태를 관리하는 인터페이스입니다.
 * <p>
 * 플래그를 영구적으로 활성화/비활성화하거나, 특정 시간 동안만 상태를 일시적으로
 * 변경하는 기능을 제공합니다.
 */
public interface FeatureFlagManager {

  /**
   * 지정된 이름의 피처 플래그 활성화 상태를 확인합니다.
   *
   * @param name         피처 플래그의 고유한 이름
   * @param defaultValue Redis에 해당 플래그 키가 없을 때 반환할 기본 상태 값
   * @return 플래그가 활성화되어 있으면 {@code true}, 그렇지 않으면 {@code false}
   */
  boolean isEnabled(@NotEmpty String name, boolean defaultValue);

  /**
   * 피처 플래그의 상태를 영구적으로 설정합니다. (TTL 없음)
   *
   * @param name    피처 플래그의 고유한 이름
   * @param enabled 활성화 상태 ({@code true}: on, {@code false}: off)
   */
  void setEnabled(@NotEmpty String name, boolean enabled);

  /**
   * 피처 플래그의 상태를 지정된 시간(TTL) 동안만 일시적으로 설정합니다.
   * <p>
   * 이 메서드는 특정 시간 동안 기능을 켜거나 끄는 'On-Timeout', 'Off-Timeout' 시나리오에 사용됩니다.
   *
   * @param name     피처 플래그의 고유한 이름
   * @param enabled  일시적으로 설정할 활성화 상태
   * @param duration 이 상태가 유지될 시간 (Time-To-Live)
   * @apiNote
   * <ul>
   * <li><b>On-Timeout:</b> 기본값이 {@code false}인 플래그를 특정 시간 동안만 켤 때 유용합니다.
   * (예: 1시간 동안 신규 기능 테스트)</li>
   * <li><b>Off-Timeout:</b> 기본값이 {@code true}인 플래그를 특정 시간 동안만 끌 때 유용합니다.
   * (예: 긴급 장애 대응으로 30분간 기능 비활성화)</li>
   * </ul>
   * TTL이 만료되어 키가 삭제되면, 이후 해당 플래그 조회 시 {@link #isEnabled(String, boolean)}에
   * 전달된 {@code defaultValue}를 기준으로 상태가 결정됩니다.
   */
  void setEnabled(@NotEmpty String name, boolean enabled, Duration duration);

  void remove(@NotEmpty String name);
}