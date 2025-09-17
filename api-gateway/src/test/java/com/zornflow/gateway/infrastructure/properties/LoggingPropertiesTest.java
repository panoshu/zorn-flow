package com.zornflow.gateway.infrastructure.properties;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class LoggingPropertiesTest {
  @Test
  void should_create_record() {
    LoggingProperties props = new LoggingProperties(
      true, List.of("/health"), "kafka", true);
    assertThat(props.publisher()).isEqualTo("kafka");
    assertThat(props.includePayload()).isTrue();
  }
}
