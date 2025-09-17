package com.zornflow.gateway.application;


import com.zornflow.gateway.domain.spi.ReplayCache;
import com.zornflow.gateway.infrastructure.properties.ReplayProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ReplayProtectionServiceTest.TestCfg.class,initializers = ConfigDataApplicationContextInitializer.class)
@TestPropertySource(locations = "classpath:application.yml")
class ReplayProtectionServiceTest {

  @Configuration
  @EnableConfigurationProperties(ReplayProperties.class)
  static class TestCfg {

    @Bean                      // 内存实现，测试专用
    public InMemoryReplayCache replayCache() {
      return new InMemoryReplayCache();
    }

    @Bean
    ReplayProtectionService service(ReplayProperties props, InMemoryReplayCache cache) {
      return new ReplayProtectionService(props,cache);
    }
  }

  public static class InMemoryReplayCache implements ReplayCache {
    private final Map<String, Boolean> store = new ConcurrentHashMap<>();

    @Override
    public Mono<Boolean> putIfAbsent(String nonce, Duration ttl) {
      return Mono.fromSupplier(() -> store.putIfAbsent(nonce, true) == null);
    }

    public void clear() {
      store.clear();
    }
  }

  @Autowired
  ReplayProtectionService securityGatewayService;
  @Autowired
  private ReplayProperties replayProps;        // ④ 配置属性已绑定好

  @Mock
  private ReplayCache mockReplayCache;

  @BeforeEach
  void setUp() {
  }

  private MockServerWebExchange createExchange(String nonce, String timestamp) {
    MockServerHttpRequest.BodyBuilder requestBuilder = MockServerHttpRequest.post("/test");
    if (nonce != null) {
      requestBuilder.header("X-Nonce", nonce);
    }
    if (timestamp != null) {
      requestBuilder.header("X-Timestamp", timestamp);
    }
    return MockServerWebExchange.from(requestBuilder.build());
  }

  @Test
  void performPreChecks_withValidNonceAndTimestamp_shouldSucceed() {
    String nonce = UUID.randomUUID().toString();
    String timestamp = String.valueOf(Instant.now().toEpochMilli());
    MockServerWebExchange exchange = createExchange(nonce, timestamp);

    when(mockReplayCache.putIfAbsent(nonce, replayProps.ttl())).thenReturn(Mono.just(true));

    StepVerifier.create(securityGatewayService.performPreChecks(exchange))
      .verifyComplete();
  }

  @Test
  void performPreChecks_withMissingNonceHeader_shouldFail() {
    String timestamp = String.valueOf(Instant.now().toEpochMilli());
    MockServerWebExchange exchange = createExchange(null, timestamp);

    StepVerifier.create(securityGatewayService.performPreChecks(exchange))
      .expectErrorSatisfies(error -> {
        assertThat(error).isInstanceOf(ResponseStatusException.class);
        ResponseStatusException ex = (ResponseStatusException) error;
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason()).contains("Missing X-Nonce");
      })
      .verify();
  }

  @Test
  void performPreChecks_withExpiredTimestamp_shouldFail() {
    String nonce = UUID.randomUUID().toString();
    String timestamp = String.valueOf(Instant.now().toEpochMilli() - replayProps.ttl().toMillis() - 1000);
    MockServerWebExchange exchange = createExchange(nonce, timestamp);

    StepVerifier.create(securityGatewayService.performPreChecks(exchange))
      .expectErrorSatisfies(error -> {
        assertThat(error).isInstanceOf(ResponseStatusException.class);
        ResponseStatusException ex = (ResponseStatusException) error;
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getReason()).contains("Request timestamp expired");
      })
      .verify();
  }

  @Test
  void performPreChecks_withReplayedNonce_shouldFail() {
    String nonce = UUID.randomUUID().toString();   // 固定 nonce
    String timestamp = String.valueOf(Instant.now().toEpochMilli());
    var exchange = createExchange(nonce, timestamp);

    System.out.println("第一次 nonce = " + nonce);
    // 第一次通过
    StepVerifier.create(securityGatewayService.performPreChecks(exchange))
      .verifyComplete();

    System.out.println("第二次 nonce = " + nonce);
    // 同样 nonce 第二次应该失败
    StepVerifier.create(securityGatewayService.performPreChecks(exchange))
      .expectErrorMatches(err ->
      {
        if (!(err instanceof ResponseStatusException)) {
          return false;
        }
        Assertions.assertNotNull(((ResponseStatusException) err).getReason());
        return ((ResponseStatusException) err).getReason().contains("Replay attack detected");
      })
      .verify();
  }
}
