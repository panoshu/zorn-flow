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

@DisplayName("ProcessPersistenceMapper 单源映射单元测试")
@SpringJUnitConfig
class ProcessPersistenceMapperTest {

  @Autowired
  private ProcessPersistenceMapper mapper;

  @Test
  @DisplayName("toDto: 应能将 ChainNodesRecord (实例) 正确映射到 DTO")
  void shouldMapChainNodeRecordToDto() {
    // Arrange
    ChainNodesRecord instance = new ChainNodesRecord();
    instance.setId("instance-node-1");
    instance.setName("Instance Node");
    instance.setNodeType("APPROVAL");
    instance.setNextNodeId("next-node");
    instance.setRuleChainId("instance-rule-chain");
    instance.setSharedNodeId("shared-node-1");
    instance.setProperties(JSONB.valueOf("{\"approver\":\"user1\"}"));

    // Act
    ProcessNodeConfig result = mapper.toDto(instance);

    // Assert
    assertThat(result.id()).isEqualTo("instance-node-1");
    assertThat(result.name()).isEqualTo("Instance Node");
    assertThat(result.type()).isEqualTo(ProcessNodeConfig.NodeType.APPROVAL);
    assertThat(result.next()).isEqualTo("next-node");
    assertThat(result.ruleChain()).isEqualTo("instance-rule-chain");
    assertThat(result.sharedNodeId()).hasValue("shared-node-1");
    assertThat(result.properties()).containsEntry("approver", "user1");
  }

  @Test
  @DisplayName("toDto: 应能将 SharedNodesRecord (模板) 正确映射到 DTO")
  void shouldMapSharedNodeRecordToDto() {
    // Arrange
    SharedNodesRecord template = new SharedNodesRecord();
    template.setId("shared-node-1");
    template.setName("Shared Node Name");
    template.setNodeType("BUSINESS");
    template.setRuleChainId("shared-rule-chain");
    template.setRecordStatus("ACTIVE");
    template.setProperties(JSONB.valueOf("{\"timeout\":5000}"));

    // Act
    ProcessNodeConfig result = mapper.toDto(template);

    // Assert
    assertThat(result.id()).isEqualTo("shared-node-1");
    assertThat(result.name()).isEqualTo("Shared Node Name");
    assertThat(result.type()).isEqualTo(ProcessNodeConfig.NodeType.BUSINESS);
    assertThat(result.ruleChain()).isEqualTo("shared-rule-chain");
    assertThat(result.status()).isEqualTo("ACTIVE");
    assertThat(result.sharedNodeId()).isEmpty(); // `sharedNodeId` in DTO is for instance's reference
    assertThat(result.next()).isNull(); // `next` is not part of the template
    assertThat(result.properties()).containsEntry("timeout", 5000);
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
      .sharedNodeId(Optional.of("shared-node-ref"))
      .build();

    // Act
    ChainNodesRecord record = mapper.toRecord(dto, "proc-xyz", 3);

    // Assert
    assertThat(record.getId()).isEqualTo("dto-node-1");
    assertThat(record.getProcessChainId()).isEqualTo("proc-xyz");
    assertThat(record.getSequence()).isEqualTo(3);
    assertThat(record.getName()).isEqualTo("DTO Node");
    assertThat(record.getNodeType()).isEqualTo("APPROVAL");
    assertThat(record.getNextNodeId()).isEqualTo("end-node");
    assertThat(record.getSharedNodeId()).isEqualTo("shared-node-ref");
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
