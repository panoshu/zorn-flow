package com.zornflow.gateway.infrastructure.properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/9/16 18:50
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SecurityPropertiesLoadTest.TestCfg.class,
  initializers = ConfigDataApplicationContextInitializer.class)
@TestPropertySource(locations = "classpath:application.yml")
class SecurityPropertiesLoadTest {

  @Autowired
  SecurityRule securityRule;

  @Test
  void should_apply_crypto_with_real_config() {
    MockServerWebExchange ex = MockServerWebExchange.builder(
      MockServerHttpRequest.get("/api").build()).build();

    assertThat(securityRule.shouldSkipCrypto(ex)).isTrue();
  }

  @Test
  void should_skip_crypto_when_path_excluded() {
    MockServerWebExchange ex = MockServerWebExchange.builder(
      MockServerHttpRequest.get("/skip").build()).build();

    assertThat(securityRule.shouldApplyCrypto(ex)).isTrue();
  }

  /* -------------------------- 全局开关 -------------------------- */
  @Test
  void global_should_enable_by_default() {
    var ex = createExchange("/api");
    assertThat(securityRule.isGlobalSecurityEnabled(ex)).isTrue();
  }

  @Test
  void global_should_skip_excluded_path() {
    var ex = createExchange("/actuator/health");
    assertThat(securityRule.isGlobalSecurityEnabled(ex)).isFalse();
  }

  /* -------------------------- 加密模块 -------------------------- */
  @Test
  void crypto_should_apply_when_path_not_excluded() {
    var ex = createExchange("/api/v1/order");
    assertThat(securityRule.shouldApplyCrypto(ex)).isTrue();
  }

  @Test
  void crypto_should_skip_excluded_path() {
    var ex = createExchange("/api/v1/files/download/123");
    assertThat(securityRule.shouldSkipCrypto(ex)).isTrue();
  }

  @Test
  void crypto_max_body_size_should_be_10mb() {
    assertThat(securityRule.maxBodySize()).isEqualTo(10 * 1024 * 1024);
  }

  @Test
  void crypto_fail_strategy_should_be_fail() {
    assertThat(securityRule.onEncryptFailure()).isEqualTo(CryptoProperties.EncryptFailureStrategy.FAIL);
  }

  /* -------------------------- 防重放 -------------------------- */
  @Test
  void replay_should_apply_when_path_not_excluded() {
    var ex = createExchange("/api/v1/pay");
    assertThat(securityRule.shouldApplyReplayProtection(ex)).isTrue();
  }

  @Test
  void replay_should_skip_excluded_path() {
    var ex = createExchange("/api/v1/public/status");
    assertThat(securityRule.shouldSkipReplayProtection(ex)).isTrue();
  }

  /* -------------------------- 日志模块 -------------------------- */
  @Test
  void log_should_apply_when_path_not_excluded() {
    var ex = createExchange("/api/v1/order");
    assertThat(securityRule.shouldApplyLogging(ex)).isTrue();
  }

  @Test
  void log_should_skip_excluded_path() {
    var ex = createExchange("/actuator/health/liveness");
    assertThat(securityRule.shouldSkipLogging(ex)).isTrue();
  }

  /* -------------------------- 工具方法 -------------------------- */
  private MockServerWebExchange createExchange(String path) {
    return MockServerWebExchange.builder(
        MockServerHttpRequest.get(path).build())
      .build();
  }


  /* ---------- 配置类 ---------- */
  @Configuration
  @EnableConfigurationProperties({
    GlobalProperties.class,
    ReplayProperties.class,
    CryptoProperties.class,
    LoggingProperties.class
  })
  static class TestCfg {

    @Bean
    SecurityRule securityRule(
      GlobalProperties global,
      ReplayProperties replay,
      LoggingProperties log,
      CryptoProperties crypto) {
      return SecurityRule.of(global, replay, log, crypto);
    }
  }
}
