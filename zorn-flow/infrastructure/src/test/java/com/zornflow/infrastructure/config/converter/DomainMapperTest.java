package com.zornflow.infrastructure.config.converter;

import com.zornflow.domain.process.entity.ProcessChain;
import com.zornflow.domain.process.entity.ProcessNode;
import com.zornflow.domain.process.types.ProcessNodeId;
import com.zornflow.domain.process.valueobject.NodeType;
import com.zornflow.domain.rule.entity.Rule;
import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.domain.rule.types.HandlerType;
import com.zornflow.domain.rule.types.Priority;
import com.zornflow.infrastructure.config.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 领域映射器单元测试
 * 验证拆分后的映射器的正确性
 *
 * @author Zorn Flow Team
 * @version 1.0
 * @since 2025/8/29
 */
class DomainMapperTest {

  private RuleMapper ruleMapper;
  private ProcessMapper processMapper;

  @BeforeEach
  void setUp() {
    ruleMapper = Mappers.getMapper(RuleMapper.class);
    processMapper = Mappers.getMapper(ProcessMapper.class);
  }

  @Test
  void shouldMapRuleChainConfigToRuleChain() {
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
    assertEquals("chain-001", result.getId().value());
    assertEquals("Test Chain", result.getName().value());
    assertEquals("1.0.0", result.getVersion().value());
    assertEquals("Test rule chain", result.getDescription());
    assertEquals("CONFIG", result.getSource());
    assertEquals(1, result.getRuleCount());

    Rule rule = result.getRules().getFirst();
    assertEquals("rule-001", rule.getId().value());
    assertEquals("Test Rule", rule.getName().value());
    assertEquals(Priority.of(10), rule.getPriority());
    assertEquals("#{context.amount > 1000}", rule.getCondition().expression());
    assertEquals(HandlerType.CLASS, rule.getHandlerConfig().type());
    assertEquals("com.example.TestHandler", rule.getHandlerConfig().handler());
  }

  @Test
  void shouldMapProcessChainConfigToProcessChain() {
    // Given
    ProcessNodeConfig nodeConfig = ProcessNodeConfig.builder()
      .id("node-001")
      .name("Test Node")
      .type(ProcessNodeConfig.NodeType.BUSINESS)
      .ruleChain("rule-chain-001")
      .next("node-002")
      .properties(Map.of("timeout", 30))
      .conditions(List.of())
      .build();

    ProcessChainConfig chainConfig = ProcessChainConfig.builder()
      .id("process-001")
      .name("Test Process")
      .version("2.0.0")
      .description("Test process chain")
      .nodes(List.of(nodeConfig))
      .build();

    // When
    ProcessChain result = processMapper.toProcessChain(chainConfig);

    // Then
    assertNotNull(result);
    assertEquals("process-001", result.getId().value());
    assertEquals("Test Process", result.getName().value());
    assertEquals("2.0.0", result.getVersion().value());
    assertEquals("Test process chain", result.getDescription());
    assertEquals("CONFIG", result.getSource());
    assertEquals(1, result.getAllNodes().size());

    ProcessNode node = result.getAllNodes().getFirst();
    assertEquals("node-001", node.getId().value());
    assertEquals("Test Node", node.getName().value());
    assertEquals(NodeType.BUSINESS, node.getType());
    assertEquals("rule-chain-001", node.getRuleChainId().value());
    assertEquals(ProcessNodeId.of("node-002"), node.getNextNodeId());
    assertEquals(30, (Integer) node.getProperty("timeout"));
  }

  @Test
  void shouldHandleNullValues() {
    // Given
    RuleChainConfig chainConfig = RuleChainConfig.builder()
      .id("chain-002")
      .name(null)  // null name
      .version(null)  // null version
      .description(null)  // null description
      .rules(List.of())
      .build();

    // When
    RuleChain result = ruleMapper.toRuleChain(chainConfig);

    // Then
    assertNotNull(result);
    assertEquals("chain-002", result.getId().value());
    assertNotNull(result.getName());  // Should have default name
    assertEquals("1.0.0", result.getVersion().value());  // Should have default version
    assertEquals("", result.getDescription());  // Should be empty string
    assertEquals(0, result.getRuleCount());
  }

  @Test
  void shouldMapGatewayConditions() {
    // Given
    GatewayConditionConfig conditionConfig = new GatewayConditionConfig(
      "#{request.approved == true}",
      "approved-node"
    );

    // When
    var result = processMapper.toGatewayCondition(conditionConfig);

    // Then
    assertNotNull(result);
    assertEquals("#{request.approved == true}", result.condition().expression());
    assertEquals("approved-node", result.nextNodeId().value());
  }

  @Test
  void shouldMapHandlerTypes() {
    // Test all handler type mappings
    assertEquals(HandlerType.CLASS, ruleMapper.configHandlerTypeToHandlerType(RuleConfig.Handler.Type.CLASS));
    assertEquals(HandlerType.SCRIPT, ruleMapper.configHandlerTypeToHandlerType(RuleConfig.Handler.Type.SCRIPT));
    assertEquals(HandlerType.JAR, ruleMapper.configHandlerTypeToHandlerType(RuleConfig.Handler.Type.JAR));
    assertEquals(HandlerType.CLASS, ruleMapper.configHandlerTypeToHandlerType(null));  // Default
  }

  @Test
  void shouldMapNodeTypes() {
    // Test all node type mappings
    assertEquals(NodeType.BUSINESS, processMapper.configNodeTypeToNodeType(ProcessNodeConfig.NodeType.BUSINESS));
    assertEquals(NodeType.APPROVAL, processMapper.configNodeTypeToNodeType(ProcessNodeConfig.NodeType.APPROVAL));
    assertEquals(NodeType.GATEWAY, processMapper.configNodeTypeToNodeType(ProcessNodeConfig.NodeType.GATEWAY));
    assertEquals(NodeType.BUSINESS, processMapper.configNodeTypeToNodeType(null));  // Default
  }

  @Test
  void shouldHandleEmptyParameterMap() {
    // Test parameter map handling
    assertNotNull(ruleMapper.safeParameterMap(null));
    assertTrue(ruleMapper.safeParameterMap(null).isEmpty());

    Map<String, Object> params = Map.of("key", "value");
    Map<String, Object> result = ruleMapper.safeParameterMap(params);
    assertNotNull(result);
    assertEquals("value", result.get("key"));
    assertNotSame(params, result);  // Should be a copy
  }

  @Test
  void shouldConvertPriority() {
    assertEquals(Priority.of(50), ruleMapper.integerToPriority(50));
    assertEquals(Priority.defaultPriority(), ruleMapper.integerToPriority(null));
  }
}
