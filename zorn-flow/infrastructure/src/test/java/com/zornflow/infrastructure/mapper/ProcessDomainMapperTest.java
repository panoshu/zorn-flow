package com.zornflow.infrastructure.mapper;

import com.zornflow.domain.process.entity.ProcessChain;
import com.zornflow.domain.process.entity.ProcessNode;
import com.zornflow.domain.process.types.ProcessChainId;
import com.zornflow.domain.process.types.ProcessChainName;
import com.zornflow.domain.process.types.ProcessNodeId;
import com.zornflow.domain.process.types.ProcessNodeName;
import com.zornflow.domain.process.valueobject.NodeType;
import com.zornflow.domain.rule.types.RuleChainId;
import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.model.ProcessNodeConfig;
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

@DisplayName("ProcessConfigMapper 单元测试 (使用 Spring Context)")
// 1. 关键修正：在 classes 数组中，使用 .class 来引用 MapStruct 生成的【实现类】，而不是接口
@SpringJUnitConfig
class ProcessDomainMapperTest {

  // 4. 像往常一样，面向接口进行注入。
  //    Spring 容器会因为上面的扫描，自动注入 ProcessConfigMapperImpl 的实例。
  //    这里不再有任何对 ...Impl 类的直接引用！
  @Autowired
  private ProcessDomainMapper mapper;

  @Test
  @DisplayName("toDomain: 应能将 ProcessChainConfig 正确映射到 ProcessChain 领域实体")
  void shouldMapProcessChainConfigToDomain() {
    // Arrange
    ProcessNodeConfig nodeConfig = ProcessNodeConfig.builder()
      .id("node-1")
      .name("First Node")
      .type(ProcessNodeConfig.NodeType.BUSINESS)
      .ruleChain("rc-1")
      .next("node-2")
      .properties(Map.of("key", "value"))
      .build();

    ProcessChainConfig config = ProcessChainConfig.builder()
      .id("proc-1")
      .name("My Process")
      .description("A test process")
      .nodes(List.of(nodeConfig))
      .build();

    // Act
    ProcessChain domain = mapper.toDomain(config);

    // Assert
    assertThat(domain).isNotNull();
    assertThat(domain.getId().value()).isEqualTo("proc-1");
    assertThat(domain.getName().value()).isEqualTo("My Process");
    assertThat(domain.getDescription()).isEqualTo("A test process");
    assertThat(domain.getAllNodes()).hasSize(1);

    ProcessNode domainNode = domain.getNodeByStringId("node-1");
    assertThat(domainNode.getName().value()).isEqualTo("First Node");
    assertThat(domainNode.getType()).isEqualTo(NodeType.BUSINESS);
    assertThat(domainNode.getRuleChainId().value()).isEqualTo("rc-1");
    assertThat(domainNode.getNextNodeId().value()).isEqualTo("node-2");
    assertThat(domainNode.getProperties()).containsEntry("key", "value");
  }

  @Test
  @DisplayName("toDto: 应能将 ProcessChain 领域实体正确映射到 ProcessChainConfig DTO")
  void shouldMapProcessChainDomainToConfig() {
    // Arrange
    ProcessNode node = ProcessNode.builder()
      .id(ProcessNodeId.of("node-1"))
      .name(ProcessNodeName.of("Domain Node"))
      .type(NodeType.APPROVAL)
      .nextNodeId(ProcessNodeId.of("node-2"))
      .ruleChainId(RuleChainId.of("rc-2"))
      .conditions(Collections.emptyList())
      .properties(Collections.emptyMap())
      .build();

    ProcessChain domain = ProcessChain.builder()
      .id(ProcessChainId.of("proc-2"))
      .name(ProcessChainName.of("Domain Process"))
      .description("Description from domain")
      .nodes(List.of(node))
      .build();

    // Act
    ProcessChainConfig config = mapper.toDto(domain);

    // Assert
    assertThat(config).isNotNull();
    assertThat(config.id()).isEqualTo("proc-2");
    assertThat(config.name()).isEqualTo("Domain Process");
    assertThat(config.nodes()).hasSize(1);

    ProcessNodeConfig nodeConfig = config.nodes().getFirst();
    assertThat(nodeConfig.id()).isEqualTo("node-1");
    assertThat(nodeConfig.name()).isEqualTo("Domain Node");
    assertThat(nodeConfig.type()).isEqualTo(ProcessNodeConfig.NodeType.APPROVAL);
    assertThat(nodeConfig.next()).isEqualTo("node-2");
    assertThat(nodeConfig.ruleChain()).isEqualTo("rc-2");

    assertThat(config.createdAt()).isNotNull();
    assertThat(config.updatedAt()).isNotNull();
  }

  // 2. 在测试类内部定义一个静态的配置类
  //    这个配置类只为本次测试服务
  @Configuration
  // 3. 核心：使用 @ComponentScan 告诉 Spring 去扫描指定的包
  //    Spring 会自动找到 ProcessConfigMapperImpl 和 CommonTypeMapper
  @ComponentScan("com.zornflow.infrastructure.mapper")
  static class TestConfig {
  }
}
