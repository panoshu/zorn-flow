package com.zornflow.infrastructure.converter;

import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.infrastructure.config.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 映射器集成测试
 * 验证MapStruct映射器是否能够正确工作
 *
 * @author Zorn Flow Team
 * @version 1.0
 * @since 2025/8/29
 */
class MapperIntegrationTest {

    private RuleMapper ruleMapper;

    @BeforeEach
    void setUp() {
        ruleMapper = Mappers.getMapper(RuleMapper.class);
    }

    @Test
    void shouldMapRuleChainCorrectly() {
        // Given
        RuleConfig.Handler handler = new RuleConfig.Handler(
            RuleConfig.Handler.Type.CLASS,
            "com.example.TestHandler",
            Map.of("param1", "value1")
        );

        RuleConfig ruleConfig = RuleConfig.builder()
            .id("rule-001")
            .name("Test Rule")
            .priority(10)
            .condition("#{context.amount > 1000}")
            .handle(handler)
            .build();

        RuleChainConfig chainConfig = RuleChainConfig.builder()
            .id("chain-001")
            .name("Test Chain")
            .version("1.0.0")
            .description("Test rule chain")
            .rules(List.of(ruleConfig))
            .build();

        // When
        RuleChain result = ruleMapper.toRuleChain(chainConfig);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("chain-001", result.getId().value());
        assertEquals("Test Chain", result.getName().value());
        assertEquals("1.0.0", result.getVersion().value());
        assertEquals("Test rule chain", result.getDescription());
        assertEquals("CONFIG", result.getSource());
        assertEquals(1, result.getRuleCount());

        // 验证嵌套的Rule对象
        List<com.zornflow.domain.rule.entity.Rule> rules = result.getRules();
        assertEquals(1, rules.size());

        com.zornflow.domain.rule.entity.Rule rule = rules.
          getFirst();
        assertNotNull(rule.getId());
        assertEquals("rule-001", rule.getId().value());
        assertEquals("Test Rule", rule.getName().value());
    }
}
