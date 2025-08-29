package com.zornflow.infrastructure.config.converter;

import com.zornflow.domain.process.entity.ProcessChain;
import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.model.ProcessNodeConfig;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.model.RuleConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

/**
 * ConfigConverter单元测试
 * 验证配置转换服务的正确性
 * 注意：使用真实地映射器来测试实际的转换逻辑，确保测试的有效性
 *
 * @author Zorn Flow Team
 * @version 1.0
 * @since 2025/8/29
 */
@ExtendWith(MockitoExtension.class)
class ConfigConverterTest {

  // 使用真实地映射器进行测试
  private RuleMapper ruleMapper;

  // 保留一些Mock对象用于特定的异常测试场景
  @Mock
  private RuleMapper mockRuleMapper;

  @Mock
  private ProcessMapper mockProcessMapper;

  private ConfigConverter converter;
  private ConfigConverter mockConverter;

  @BeforeEach
  void setUp() {
    // 初始化真实地映射器
    ruleMapper = Mappers.getMapper(RuleMapper.class);
    ProcessMapper processMapper = Mappers.getMapper(ProcessMapper.class);

    // 创建使用真实映射器的转换器
    converter = new ConfigConverter(ruleMapper, processMapper);

    // 创建使用Mock映射器的转换器（用于异常测试）
    mockConverter = new ConfigConverter(mockRuleMapper, mockProcessMapper);
  }

  @Test
  void shouldConvertValidRuleChainConfig() {
    // Given
    RuleChainConfig config = createValidRuleChainConfig();

    // When
    Optional<RuleChain> result = converter.convertRuleChain(config);

    // Then
    assertTrue(result.isPresent());
    RuleChain ruleChain = result.get();
    assertEquals("chain-001", ruleChain.getId().value());
    assertEquals("Test Chain", ruleChain.getName().value());
    assertEquals(1, ruleChain.getRuleCount());
  }

  @Test
  void shouldReturnEmptyForNullRuleChainConfig() {
    // When
    Optional<RuleChain> result = converter.convertRuleChain(null);

    // Then
    assertTrue(result.isEmpty());
    // 注意：这里不需要verify，因为使用的是真实映射器
  }

  @Test
  void shouldHandleRuleChainConversionException() {
    // Given - 使用Mock映射器来模拟异常
    RuleChainConfig config = createValidRuleChainConfig();
    when(mockRuleMapper.toRuleChain(config)).thenThrow(new IllegalArgumentException("Invalid config"));

    // When & Then
    assertThrows(ConfigConverter.ConfigConverterException.class,
      () -> mockConverter.convertRuleChain(config));
  }

  @Test
  void shouldConvertRuleChainList() {
    // Given
    RuleChainConfig config1 = createValidRuleChainConfig();
    RuleChainConfig config2 = createValidRuleChainConfig();
    List<RuleChainConfig> configs = List.of(config1, config2);

    // When
    List<RuleChain> result = converter.convertRuleChains(configs);

    // Then
    assertEquals(2, result.size());
    assertEquals("chain-001", result.get(0).getId().value());
    assertEquals("chain-001", result.get(1).getId().value());
  }

  @Test
  void shouldSkipInvalidRuleChainInBatch() {
    // Given - 使用Mock映射器来模拟部分失败的场景
    RuleChainConfig validConfig = createValidRuleChainConfig();
    RuleChainConfig invalidConfig = createDifferentRuleChainConfig(); // 创建不同的配置
    List<RuleChainConfig> configs = List.of(validConfig, invalidConfig);

    // 模拟一个成功，一个失败 - 使用argThat来避免对象引用问题，添加null检查
    when(mockRuleMapper.toRuleChain(argThat(config ->
      config != null && "chain-001".equals(config.id()))))
      .thenReturn(createMockRuleChain());
    when(mockRuleMapper.toRuleChain(argThat(config ->
      config != null && "chain-002".equals(config.id()))))
      .thenThrow(new IllegalArgumentException("Invalid"));

    // When
    List<RuleChain> result = mockConverter.convertRuleChains(configs);

    // Then
    assertEquals(1, result.size());  // Only valid config should be converted
    assertEquals("mock-chain-001", result.getFirst().getId().value());
  }

  @Test
  void shouldConvertValidProcessChainConfig() {
    // Given
    ProcessChainConfig config = createValidProcessChainConfig();

    // When
    Optional<ProcessChain> result = converter.convertProcessChain(config);

    // Then
    assertTrue(result.isPresent());
    ProcessChain processChain = result.get();
    assertEquals("process-001", processChain.getId().value());
    assertEquals("Test Process", processChain.getName().value());
    assertEquals(1, processChain.getAllNodes().size());
  }

  @Test
  void shouldReturnEmptyForNullProcessChainConfig() {
    // When
    Optional<ProcessChain> result = converter.convertProcessChain(null);

    // Then
    assertTrue(result.isEmpty());
    // 注意：这里不需要verify，因为使用的是真实映射器
  }

  @Test
  void shouldReturnTrueForReadinessCheck() {
    // When
    boolean ready = converter.isReady();

    // Then
    assertTrue(ready);
  }

  @Test
  void shouldReturnStatus() {
    // When
    String status = converter.getStatus();

    // Then
    assertNotNull(status);
    assertTrue(status.contains("ConfigConverter"));
    assertTrue(status.contains("ready=true"));
  }

  @Test
  void shouldHandleEmptyRuleChainList() {
    // When
    List<RuleChain> result = converter.convertRuleChains(List.of());

    // Then
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldHandleNullRuleChainList() {
    // When
    List<RuleChain> result = converter.convertRuleChains(null);

    // Then
    assertTrue(result.isEmpty());
  }

  private RuleChainConfig createValidRuleChainConfig() {
    RuleConfig.Handler handler = new RuleConfig.Handler(
      RuleConfig.Handler.Type.CLASS,
      "com.example.TestHandler",
      Map.of()
    );

    RuleConfig ruleConfig = RuleConfig.builder()
      .id("rule-001")
      .name("Test Rule")
      .priority(10)
      .condition("#{true}")
      .handle(handler)
      .build();

    return RuleChainConfig.builder()
      .id("chain-001")
      .name("Test Chain")
      .version("1.0.0")
      .description("Test rule chain")
      .rules(List.of(ruleConfig))
      .build();
  }

  /**
   * 创建一个不同的规则链配置，用于测试异常处理
   */
  private RuleChainConfig createDifferentRuleChainConfig() {
    RuleConfig.Handler handler = new RuleConfig.Handler(
      RuleConfig.Handler.Type.SCRIPT,
      "com.example.InvalidHandler",
      Map.of()
    );

    RuleConfig ruleConfig = RuleConfig.builder()
      .id("rule-002")
      .name("Invalid Rule")
      .priority(20)
      .condition("#{false}")
      .handle(handler)
      .build();

    return RuleChainConfig.builder()
      .id("chain-002")
      .name("Invalid Chain")
      .version("2.0.0")
      .description("Invalid rule chain")
      .rules(List.of(ruleConfig))
      .build();
  }

  private ProcessChainConfig createValidProcessChainConfig() {
    ProcessNodeConfig nodeConfig = ProcessNodeConfig.builder()
      .id("node-001")
      .name("Test Node")
      .type(ProcessNodeConfig.NodeType.BUSINESS)
      .ruleChain("rule-chain-001")
      .properties(Map.of())
      .conditions(List.of())
      .build();

    return ProcessChainConfig.builder()
      .id("process-001")
      .name("Test Process")
      .version("1.0.0")
      .description("Test process chain")
      .nodes(List.of(nodeConfig))
      .build();
  }

  /**
   * 创建一个Mock RuleChain对象，用于测试异常处理场景
   */
  private RuleChain createMockRuleChain() {
    // 使用真实的映射器创建一个简单的RuleChain作为Mock的返回值
    RuleConfig.Handler handler = new RuleConfig.Handler(
      RuleConfig.Handler.Type.CLASS,
      "com.example.MockHandler",
      Map.of()
    );

    RuleConfig ruleConfig = RuleConfig.builder()
      .id("mock-rule-001")
      .name("Mock Rule")
      .priority(10)
      .condition("#{true}")
      .handle(handler)
      .build();

    RuleChainConfig chainConfig = RuleChainConfig.builder()
      .id("mock-chain-001")
      .name("Mock Chain")
      .version("1.0.0")
      .description("Mock rule chain")
      .rules(List.of(ruleConfig))
      .build();

    return ruleMapper.toRuleChain(chainConfig);
  }
}
