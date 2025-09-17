package com.zornflow.gateway.infrastructure.properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.util.unit.DataSize;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityRuleTest {
  private SecurityRule rule;

  @BeforeEach
  void setUp() {
    GlobalProperties global = new GlobalProperties(true, List.of("/global"));
    ReplayProperties replay = new ReplayProperties(true, List.of("/open"), "nonce", Duration.ofMinutes(5), "n:");
    LoggingProperties log = new LoggingProperties(true, List.of(), "kafka", true);
    CryptoProperties crypto = new CryptoProperties(
      true, List.of("/skip"), "AES", "static", "k",
      DataSize.ofMegabytes(1), CryptoProperties.EncryptFailureStrategy.FAIL, "cache");
    rule = SecurityRule.of(global, replay, log, crypto);
  }

  @Test
  void should_apply_crypto_when_path_not_excluded() {
    var exchange = createExchange("/api");
    assertThat(rule.shouldApplyCrypto(exchange)).isTrue();
  }

  @Test
  void should_skip_crypto_when_path_excluded() {
    var exchange = createExchange("/skip");
    assertThat(rule.shouldApplyCrypto(exchange)).isFalse();
  }

  @Test
  void should_skip_all_when_global_disabled() {
    SecurityRule localRule = SecurityRule.of(
      new GlobalProperties(false, List.of()),
      new ReplayProperties(true, List.of(), "n", Duration.ZERO, ""),
      new LoggingProperties(true, List.of(), "k", true),
      new CryptoProperties(true, List.of(), "A", "s", "k", DataSize.ofBytes(1),
        CryptoProperties.EncryptFailureStrategy.FAIL, "")
    );
    var exchange = createExchange("/api");
    assertThat(localRule.shouldApplyCrypto(exchange)).isFalse();
  }

  private MockServerWebExchange createExchange(String path) {
    return MockServerWebExchange.builder(
      MockServerHttpRequest.get(path).build()
    ).build();
  }
}
