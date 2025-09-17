package com.zornflow.gateway.infrastructure.properties;

import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class CryptoPropertiesTest {
  @Test
  void should_create_record() {
    CryptoProperties props = new CryptoProperties(
      true,
      List.of("/open"),
      "AES",
      "static",
      "key1",
      DataSize.ofMegabytes(2),
      CryptoProperties.EncryptFailureStrategy.FAIL,
      "cache");
    assertThat(props.algorithmStrategy()).isEqualTo("AES");
    assertThat(props.maxBodySize().toBytes()).isEqualTo(2 * 1024 * 1024);
  }
}
