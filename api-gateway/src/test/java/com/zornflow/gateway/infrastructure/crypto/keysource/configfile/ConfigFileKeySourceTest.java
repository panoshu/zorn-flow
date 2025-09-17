package com.zornflow.gateway.infrastructure.crypto.keysource.configfile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ConfigFileKeySourceTest {

  private ConfigFileKeySource keySource;

  @BeforeEach
  void setUp() {
    var key1 = new ConfigFileCryptoConfig.KeyEntry("v1", "secret1", false);
    var key2 = new ConfigFileCryptoConfig.KeyEntry("v2", "secret2", true);
    var config = new ConfigFileCryptoConfig(List.of(key1, key2));
    keySource = new ConfigFileKeySource(config);
    keySource.init(); // Manually call init for testing
  }

  @Test
  void obtainPrimaryKey_shouldReturnV2() {
    StepVerifier.create(keySource.obtainPrimaryKey())
      .assertNext(keyDetail -> {
        assertThat(keyDetail.version()).isEqualTo("v2");
        assertThat(keyDetail.secret()).isEqualTo("secret2");
      })
      .verifyComplete();
  }

  @Test
  void obtainKey_withVersionV1_shouldReturnV1() {
    StepVerifier.create(keySource.obtainKey("any-id", "v1"))
      .expectNext("secret1")
      .verifyComplete();
  }
}
