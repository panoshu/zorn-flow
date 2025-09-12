package com.zornflow.infrastructure.persistence.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zornflow.infrastructure.config.model.RuleConfig;
import com.zornflow.infrastructure.persistence.jooq.tables.records.ChainRulesRecord;
import com.zornflow.infrastructure.persistence.jooq.tables.records.SharedRulesRecord;
import org.jooq.JSONB;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RulePersistenceMapper 单源映射单元测试")
@SpringJUnitConfig
class RulePersistenceMapperTest {

  @Autowired
  private RulePersistenceMapper mapper;

  @Test
  @DisplayName("toDto: 应能将 ChainRulesRecord (实例) 正确映射到 DTO")
  void shouldMapChainRuleRecordToDto() {
    // Arrange
    ChainRulesRecord instance = new ChainRulesRecord();
    instance.setId("instance-rule-1");
    instance.setName("Instance Rule Name");
    instance.setPriority(50);
    instance.setCondition("instance_condition");
    instance.setSharedRuleId("shared-rule-1");
    instance.setHandlerConfig(JSONB.valueOf("{\"type\":\"SCRIPT\",\"handler\":\"InstanceScript\"}"));

    // Act
    RuleConfig result = mapper.toDto(instance);

    // Assert
    assertThat(result.id()).isEqualTo("instance-rule-1");
    assertThat(result.name()).isEqualTo("Instance Rule Name");
    assertThat(result.priority()).isEqualTo(50);
    assertThat(result.condition()).isEqualTo("instance_condition");
    assertThat(result.sharedRuleId()).hasValue("shared-rule-1");
    assertThat(result.handle().type()).isEqualTo(RuleConfig.HandlerConfig.Type.SCRIPT);
    assertThat(result.handle().handler()).isEqualTo("InstanceScript");
  }

  @Test
  @DisplayName("toDto: 应能将 SharedRulesRecord (模板) 正确映射到 DTO")
  void shouldMapSharedRuleRecordToDto() {
    // Arrange
    SharedRulesRecord template = new SharedRulesRecord();
    template.setId("shared-rule-1");
    template.setName("Shared Rule Name");
    template.setPriority(100);
    template.setCondition("shared_condition");
    template.setRecordStatus("ACTIVE");
    template.setHandlerConfig(JSONB.valueOf("{\"type\":\"CLASS\",\"handler\":\"SharedHandler\"}"));

    // Act
    RuleConfig result = mapper.toDto(template);

    // Assert
    assertThat(result.id()).isEqualTo("shared-rule-1");
    assertThat(result.name()).isEqualTo("Shared Rule Name");
    assertThat(result.priority()).isEqualTo(100);
    assertThat(result.condition()).isEqualTo("shared_condition");
    assertThat(result.status()).isEqualTo("ACTIVE");
    assertThat(result.sharedRuleId()).isEmpty();
    assertThat(result.handle().type()).isEqualTo(RuleConfig.HandlerConfig.Type.CLASS);
    assertThat(result.handle().handler()).isEqualTo("SharedHandler");
  }

  @Test
  @DisplayName("toRecord: 应能将 RuleConfig DTO 转换为 ChainRulesRecord")
  void shouldMapDtoToChainRulesRecord() {
    // Arrange
    RuleConfig dto = RuleConfig.builder()
      .id("dto-rule-1")
      .name("DTO Rule")
      .priority(150)
      .condition("dto_condition")
      .handle(new RuleConfig.HandlerConfig(RuleConfig.HandlerConfig.Type.JAR, "my.jar", Map.of("entry", "main.class")))
      .sharedRuleId(Optional.of("shared-rule-ref"))
      .build();

    // Act
    ChainRulesRecord record = mapper.toRecord(dto, "chain-abc", 5);

    // Assert
    assertThat(record.getId()).isEqualTo("dto-rule-1");
    assertThat(record.getRuleChainId()).isEqualTo("chain-abc");
    assertThat(record.getSequence()).isEqualTo(5);
    assertThat(record.getName()).isEqualTo("DTO Rule");
    assertThat(record.getPriority()).isEqualTo(150);
    assertThat(record.getCondition()).isEqualTo("dto_condition");
    assertThat(record.getSharedRuleId()).isEqualTo("shared-rule-ref");
    assertThat(record.getHandlerConfig().data()).contains("\"type\":\"JAR\"");
  }

  @Configuration
  @ComponentScan("com.zornflow.infrastructure.persistence.mapper")
  static class TestConfig {
    @Bean
    public ObjectMapper objectMapper() {
      return new ObjectMapper();
    }
  }
}
