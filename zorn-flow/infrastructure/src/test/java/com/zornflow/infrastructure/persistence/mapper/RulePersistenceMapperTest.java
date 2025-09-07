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

@DisplayName("RulePersistenceMapper 单元测试 (最佳实践)")
@SpringJUnitConfig
class RulePersistenceMapperTest {

  @Configuration
  // 扫描 mapper 包，Spring 会自动装配 RuleMapper 和它依赖的 JsonbMapperHelper
  @ComponentScan("com.zornflow.infrastructure.persistence.mapper")
  static class TestConfig {
    // JsonbMapperHelper 依赖 ObjectMapper，所以我们在这里提供一个 Bean
    @Bean
    public ObjectMapper objectMapper() {
      return new ObjectMapper();
    }
  }

  @Autowired
  private RulePersistenceMapper mapper;

  @Test
  @DisplayName("toDto: 应能合并 SharedRulesRecord 和 ChainRulesRecord 的属性")
  void shouldMergeSharedAndChainRuleRecordsToDto() {
    // Arrange
    SharedRulesRecord template = new SharedRulesRecord();
    template.setId("shared-rule-1");
    template.setName("Shared Rule Name");
    template.setPriority(100);
    template.setCondition("shared_condition");
    template.setHandlerConfig(JSONB.valueOf("{\"type\":\"CLASS\",\"handler\":\"SharedHandler\"}"));

    ChainRulesRecord instance = new ChainRulesRecord();
    instance.setId("instance-rule-1");
    instance.setName("Instance Rule Name"); // 覆盖 name
    // priority 为 null, 应使用 template 的
    instance.setHandlerConfig(null); // handlerConfig 为 null, 应使用 template 的

    // Act
    RuleConfig result = mapper.toDto(template, instance, new JsonbMapperHelper(new ObjectMapper()));

    // Assert
    assertThat(result.id()).isEqualTo("instance-rule-1");
    assertThat(result.sharedRuleId()).hasValue("shared-rule-1");
    assertThat(result.name()).isEqualTo("Instance Rule Name"); // 使用了 instance 的值
    assertThat(result.priority()).isEqualTo(100); // 使用了 template 的值
    assertThat(result.condition()).isEqualTo("shared_condition"); // 使用了 template 的值
    assertThat(result.handle().type()).isEqualTo(RuleConfig.HandlerConfig.Type.CLASS);
    assertThat(result.handle().handler()).isEqualTo("SharedHandler");
  }

  @Test
  @DisplayName("toDto: 当没有共享规则时，应能正确映射 ChainRulesRecord")
  void shouldMapChainRuleRecordWithoutTemplate() {
    // Arrange
    ChainRulesRecord instance = new ChainRulesRecord();
    instance.setId("standalone-rule-1");
    instance.setName("Standalone Rule");
    instance.setPriority(200);
    instance.setCondition("standalone_condition");
    instance.setHandlerConfig(JSONB.valueOf("{\"type\":\"SCRIPT\",\"handler\":\"my-script\"}"));

    // Act
    RuleConfig result = mapper.toDto(instance, new JsonbMapperHelper(new ObjectMapper()));

    // Assert
    assertThat(result.id()).isEqualTo("standalone-rule-1");
    assertThat(result.sharedRuleId()).isEmpty();
    assertThat(result.name()).isEqualTo("Standalone Rule");
    assertThat(result.priority()).isEqualTo(200);
    assertThat(result.condition()).isEqualTo("standalone_condition");
    assertThat(result.handle().type()).isEqualTo(RuleConfig.HandlerConfig.Type.SCRIPT);
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
      // ---  关键修正  ---
      // 确保 Optional 字段被正确初始化，而不是为 null
      .sharedRuleId(Optional.empty())
      .build();

    // Act
    ChainRulesRecord record = mapper.toRecord(dto, "chain-abc", 5, new JsonbMapperHelper(new ObjectMapper()));

    // Assert
    assertThat(record.getId()).isEqualTo("dto-rule-1");
    assertThat(record.getRuleChainId()).isEqualTo("chain-abc");
    assertThat(record.getSequence()).isEqualTo(5);
    assertThat(record.getName()).isEqualTo("DTO Rule");
    assertThat(record.getPriority()).isEqualTo(150);
    assertThat(record.getCondition()).isEqualTo("dto_condition");
    assertThat(record.getHandlerConfig().data()).contains("\"type\":\"JAR\"");
  }
}
