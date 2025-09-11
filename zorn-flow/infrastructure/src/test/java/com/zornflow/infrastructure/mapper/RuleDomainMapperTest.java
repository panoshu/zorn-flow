package com.zornflow.infrastructure.mapper;

import com.zornflow.domain.rule.entity.Rule;
import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.domain.rule.types.*;
import com.zornflow.domain.rule.valueobject.Handler;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.model.RuleConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RuleConfigMapper 单元测试 (最佳实践)")
@SpringJUnitConfig
class RuleDomainMapperTest {

  @Autowired
  private RuleDomainMapper mapper;

  @Test
  @DisplayName("toDomain: 应能将 RuleChainConfig 正确映射到 RuleChain 领域实体")
  void shouldMapRuleChainConfigToDomain() {
    // Arrange
    RuleConfig ruleConfig = RuleConfig.builder()
      .id("rule-1")
      .name("Test Rule")
      .priority(50)
      .condition("#{amount > 100}")
      .handle(new RuleConfig.HandlerConfig(RuleConfig.HandlerConfig.Type.CLASS, "com.example.Handler", Collections.emptyMap()))
      .build();

    RuleChainConfig chainConfig = RuleChainConfig.builder()
      .id("chain-1")
      .name("Test Chain")
      .description("A test rule chain")
      .rules(List.of(ruleConfig))
      .build();

    // Act
    RuleChain domain = mapper.toDomain(chainConfig);

    // Assert
    assertThat(domain).isNotNull();
    assertThat(domain.getId().value()).isEqualTo("chain-1");
    assertThat(domain.getName().value()).isEqualTo("Test Chain");
    assertThat(domain.getRules()).hasSize(1);

    Rule domainRule = domain.getRuleById(RuleId.of("rule-1")).orElseThrow();
    assertThat(domainRule.getName().value()).isEqualTo("Test Rule");
    assertThat(domainRule.getPriority().value()).isEqualTo(50);
    assertThat(domainRule.getCondition().expression()).isEqualTo("#{amount > 100}");
    assertThat(domainRule.getHandler().type()).isEqualTo(HandlerType.CLASS);
    assertThat(domainRule.getHandler().handler()).isEqualTo("com.example.Handler");
  }

  @Test
  @DisplayName("toDto: 应能将 RuleChain 领域实体正确映射到 RuleChainConfig DTO")
  void shouldMapRuleChainDomainToDto() {
    // Arrange
    Rule rule = Rule.builder()
      .id(RuleId.of("rule-2"))
      .name(RuleName.of("Domain Rule"))
      .priority(Priority.of(99))
      .condition(Condition.of("#{true}"))
      .handler(Handler.of(HandlerType.SCRIPT, "myScript", Map.of("timeout", 500)))
      .build();

    RuleChain domain = RuleChain.builder()
      .id(RuleChainId.of("chain-2"))
      .name(RuleChainName.of("Domain Chain"))
      .description("Description from domain")
      .rules(List.of(rule))
      .build();

    // Act
    RuleChainConfig dto = mapper.toDto(domain);

    // Assert
    assertThat(dto).isNotNull();
    assertThat(dto.id()).isEqualTo("chain-2");
    assertThat(dto.name()).isEqualTo("Domain Chain");
    assertThat(dto.rules()).hasSize(1);

    RuleConfig ruleDto = dto.rules().getFirst();
    assertThat(ruleDto.id()).isEqualTo("rule-2");
    assertThat(ruleDto.name()).isEqualTo("Domain Rule");
    assertThat(ruleDto.priority()).isEqualTo(99);
    assertThat(ruleDto.condition()).isEqualTo("#{true}");
    assertThat(ruleDto.handle().type()).isEqualTo(RuleConfig.HandlerConfig.Type.SCRIPT);
    assertThat(ruleDto.handle().handler()).isEqualTo("myScript");
    assertThat(ruleDto.handle().parameters()).containsEntry("timeout", 500);
  }

  @Configuration
  @ComponentScan("com.zornflow.infrastructure.mapper")
  static class TestConfig {
  }
}
