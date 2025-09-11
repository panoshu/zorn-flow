package com.zornflow.infrastructure.persistence.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zornflow.infrastructure.config.model.ProcessNodeConfig;
import com.zornflow.infrastructure.persistence.jooq.tables.records.ChainNodesRecord;
import com.zornflow.infrastructure.persistence.jooq.tables.records.SharedNodesRecord;
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

@DisplayName("ProcessPersistenceMapper 单元测试 (最佳实践)")
@SpringJUnitConfig
class ProcessPersistenceMapperTest {

  @Autowired
  private ProcessPersistenceMapper mapper;

  @Test
  @DisplayName("toDto: 应能合并 SharedNodesRecord 和 ChainNodesRecord 的属性")
  void shouldMergeSharedAndChainNodeRecordsToDto() {
    // Arrange
    SharedNodesRecord template = new SharedNodesRecord();
    template.setId("shared-node-1");
    template.setName("Shared Node Name");
    template.setNodeType("BUSINESS");
    template.setRuleChainId("shared-rule-chain");
    template.setProperties(JSONB.valueOf("{\"timeout\":5000}"));

    ChainNodesRecord instance = new ChainNodesRecord();
    instance.setId("instance-node-1");
    instance.setRuleChainId("instance-rule-chain"); // 覆盖 ruleChainId
    // 其他属性为 null，应使用 template 的
    instance.setName(null);

    // Act
    ProcessNodeConfig result = mapper.toDto(template, instance, new JsonbMapperHelper(new ObjectMapper()));

    // Assert
    assertThat(result.id()).isEqualTo("instance-node-1");
    assertThat(result.sharedNodeId()).hasValue("shared-node-1");
    assertThat(result.name()).isEqualTo("Shared Node Name"); // 使用了 template 的 name
    assertThat(result.type()).isEqualTo(ProcessNodeConfig.NodeType.BUSINESS);
    assertThat(result.ruleChain()).isEqualTo("instance-rule-chain"); // 使用了 instance 的 ruleChain
    assertThat(result.properties()).containsEntry("timeout", 5000);
  }

  @Test
  @DisplayName("toDto: 当没有共享节点时，应能正确映射 ChainNodesRecord")
  void shouldMapChainNodeRecordWithoutTemplate() {
    // Arrange
    ChainNodesRecord instance = new ChainNodesRecord();
    instance.setId("standalone-node-1");
    instance.setName("Standalone Node");
    instance.setNodeType("GATEWAY");
    instance.setNextNodeId("next-node");
    instance.setRuleChainId("standalone-rule-chain");

    // Act
    ProcessNodeConfig result = mapper.toDto(instance, new JsonbMapperHelper(new ObjectMapper()));

    // Assert
    assertThat(result.id()).isEqualTo("standalone-node-1");
    assertThat(result.sharedNodeId()).isEmpty();
    assertThat(result.name()).isEqualTo("Standalone Node");
    assertThat(result.type()).isEqualTo(ProcessNodeConfig.NodeType.GATEWAY);
    assertThat(result.next()).isEqualTo("next-node");
  }

  @Test
  @DisplayName("toRecord: 应能将 ProcessNodeConfig DTO 转换为 ChainNodesRecord")
  void shouldMapDtoToChainNodesRecord() {
    // Arrange
    ProcessNodeConfig dto = ProcessNodeConfig.builder()
      .id("dto-node-1")
      .name("DTO Node")
      .type(ProcessNodeConfig.NodeType.APPROVAL)
      .next("end-node")
      .properties(Map.of("approver", "manager"))
      // ---  关键修正  ---
      // 确保 Optional 字段被正确初始化，而不是为 null
      .sharedNodeId(Optional.empty())
      .build();

    // Act
    ChainNodesRecord record = mapper.toRecord(dto, "proc-xyz", 3, new JsonbMapperHelper(new ObjectMapper()));

    // Assert
    assertThat(record.getId()).isEqualTo("dto-node-1");
    assertThat(record.getProcessChainId()).isEqualTo("proc-xyz");
    assertThat(record.getSequence()).isEqualTo(3);
    assertThat(record.getName()).isEqualTo("DTO Node");
    assertThat(record.getNodeType()).isEqualTo("APPROVAL");
    assertThat(record.getNextNodeId()).isEqualTo("end-node");
    assertThat(record.getProperties().data()).contains("\"approver\":\"manager\"");
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
