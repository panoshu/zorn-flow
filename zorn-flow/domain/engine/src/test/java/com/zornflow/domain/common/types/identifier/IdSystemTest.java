package com.zornflow.domain.common.types.identifier;

import com.zornflow.domain.process.entity.ProcessInstance;
import com.zornflow.domain.process.types.ProcessInstanceId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdSystemTest {

  @BeforeAll
  static void setUp() {
    DomainIds.register(Map.of(
      ProcessInstanceId.class, new MockUlidStrategy()
    ));
  }

  @Test
  void generateOrderId() {
    ProcessInstanceId id1 = ProcessInstanceId.generate();
    ProcessInstanceId id2 = ProcessInstanceId.generate();
    assertThat(id1.value()).hasSize(26).startsWith("01TEST");
    assertThat(id1).isLessThan(id2);
  }

  @Test
  void restoreOrderId() {
    ProcessInstanceId id = ProcessInstanceId.of("01TEST12345678901234567890");
    assertThat(id.value()).isEqualTo("01TEST12345678901234567890");
  }

  @Test
  void invalidOrderIdShouldThrow() {
    assertThatThrownBy(() -> ProcessInstanceId.of("bad-ulid"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Invalid [ ProcessInstanceId ] [ bad-ulid ]");
  }

  @Test
  void createOrderWithGeneratedId() {
    ProcessInstanceId id = ProcessInstanceId.generate();
    ProcessInstance processInstance = ProcessInstance.builder().processInstanceId(id).build();

    assertThat(processInstance.getId().value()).startsWith("01TEST");
  }

  @Test
  void createOrderWithFixedId() {
    ProcessInstanceId fixed = ProcessInstanceId.of("01TEST12345678901234567890");
    ProcessInstance processInstance = ProcessInstance.builder().processInstanceId(fixed).build();

    assertThat(processInstance.getId().value()).isEqualTo("01TEST12345678901234567890");
  }
}
