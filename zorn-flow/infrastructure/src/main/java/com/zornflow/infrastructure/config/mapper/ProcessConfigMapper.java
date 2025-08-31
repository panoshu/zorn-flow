package com.zornflow.infrastructure.config.mapper;

import com.zornflow.domain.common.types.Version;
import com.zornflow.domain.process.entity.ProcessChain;
import com.zornflow.domain.process.entity.ProcessNode;
import com.zornflow.domain.process.types.ProcessChainId;
import com.zornflow.domain.process.types.ProcessChainName;
import com.zornflow.domain.process.types.ProcessNodeId;
import com.zornflow.domain.process.types.ProcessNodeName;
import com.zornflow.domain.process.valueobject.NodeType;
import com.zornflow.domain.process.valueobject.GatewayCondition;
import com.zornflow.domain.rule.types.Condition;
import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.model.ProcessNodeConfig;
import com.zornflow.infrastructure.config.model.GatewayConditionConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流程配置映射器
 * 将基础设施层配置DTO转换为领域层实体
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29 11:30
 */
@Mapper(config = MapstructConfig.class)
public interface ProcessConfigMapper {

  ProcessConfigMapper INSTANCE = Mappers.getMapper(ProcessConfigMapper.class);

  /**
   * 转换流程链配置为流程链实体
   */
  @Mapping(target = "id", source = "id", qualifiedByName = "stringToProcessChainId")
  @Mapping(target = "name", source = "name", qualifiedByName = "stringToProcessChainName")
  @Mapping(target = "version", source = "version", qualifiedByName = "stringToVersion")
  @Mapping(target = "source", constant = "YAML")
  @Mapping(target = "description", source = "description")
  @Mapping(target = "nodes", source = "nodes", qualifiedByName = "listNodesToMap")
  ProcessChain toProcessChain(ProcessChainConfig config);

  /**
   * 转换流程节点配置为流程节点实体
   */
  @Mapping(target = "id", source = "id", qualifiedByName = "stringToProcessNodeId")
  @Mapping(target = "name", source = "name", qualifiedByName = "stringToProcessNodeName")
  @Mapping(target = "nextNodeId", source = "next", qualifiedByName = "stringToProcessNodeId")
  @Mapping(target = "type", source = "type", qualifiedByName = "configNodeTypeToValueObject")
  @Mapping(target = "ruleChainId", source = "ruleChain", qualifiedByName = "stringToRuleChainId")
  @Mapping(target = "conditions", source = "conditions", qualifiedByName = "configConditionsToGatewayConditions")
  @Mapping(target = "properties", source = "properties")
  ProcessNode toProcessNode(ProcessNodeConfig config);

  /**
   * 批量转换流程节点
   */
  List<ProcessNode> toProcessNodes(List<ProcessNodeConfig> configs);

  // =============== 反向转换方法（领域实体到配置DTO） ===============

  /**
   * 转换流程链实体为流程链配置
   */
  @Mapping(target = "id", source = "id", qualifiedByName = "processChainIdToString")
  @Mapping(target = "name", source = "name", qualifiedByName = "processChainNameToString")
  @Mapping(target = "version", source = "version", qualifiedByName = "versionToString")
  @Mapping(target = "description", source = "description")
  @Mapping(target = "nodes", source = "nodes", qualifiedByName = "mapNodesToList")
  ProcessChainConfig toProcessChainConfig(ProcessChain processChain);

  /**
   * 转换流程节点实体为流程节点配置
   */
  @Mapping(target = "id", source = "id", qualifiedByName = "processNodeIdToString")
  @Mapping(target = "name", source = "name", qualifiedByName = "processNodeNameToString")
  @Mapping(target = "next", source = "nextNodeId", qualifiedByName = "processNodeIdToString")
  @Mapping(target = "type", source = "type", qualifiedByName = "nodeTypeToConfigType")
  @Mapping(target = "ruleChain", source = "ruleChainId", qualifiedByName = "ruleChainIdToString")
  @Mapping(target = "conditions", source = "conditions", qualifiedByName = "gatewayConditionsToConfigConditions")
  @Mapping(target = "properties", source = "properties")
  ProcessNodeConfig toProcessNodeConfig(ProcessNode processNode);

  /**
   * 批量转换流程节点（实体到配置）
   */
  List<ProcessNodeConfig> toProcessNodeConfigs(List<ProcessNode> processNodes);

  // 类型转换方法

  @Named("stringToProcessChainId")
  default ProcessChainId stringToProcessChainId(String id) {
    return id != null ? ProcessChainId.of(id) : null;
  }

  @Named("stringToProcessChainName")
  default ProcessChainName stringToProcessChainName(String name) {
    return name != null ? ProcessChainName.of(name) : null;
  }

  @Named("stringToProcessNodeId")
  default ProcessNodeId stringToProcessNodeId(String id) {
    return id != null ? ProcessNodeId.of(id) : null;
  }

  @Named("stringToProcessNodeName")
  default ProcessNodeName stringToProcessNodeName(String name) {
    return name != null ? ProcessNodeName.of(name) : null;
  }

  @Named("configNodeTypeToValueObject")
  default NodeType configNodeTypeToValueObject(ProcessNodeConfig.NodeType configType) {
    if (configType == null) {
      return NodeType.BUSINESS;
    }
    return switch (configType) {
      case BUSINESS -> NodeType.BUSINESS;
      case APPROVAL -> NodeType.APPROVAL;
      case GATEWAY -> NodeType.GATEWAY;
    };
  }

  @Named("stringToRuleChainId")
  default com.zornflow.domain.rule.types.RuleChainId stringToRuleChainId(String ruleChain) {
    return ruleChain != null ? com.zornflow.domain.rule.types.RuleChainId.of(ruleChain) : null;
  }

  @Named("configConditionsToGatewayConditions")
  default List<GatewayCondition> configConditionsToGatewayConditions(List<GatewayConditionConfig> configs) {
    if (configs == null || configs.isEmpty()) {
      return new java.util.ArrayList<>();
    }
    return configs.stream()
      .map(this::configConditionToGatewayCondition)
      .collect(java.util.stream.Collectors.toList());
  }

  default GatewayCondition configConditionToGatewayCondition(GatewayConditionConfig config) {
    Condition condition = config.condition() != null ? Condition.of(config.condition()) : null;
    ProcessNodeId nextNodeId = config.next() != null ? ProcessNodeId.of(config.next()) : null;
    return GatewayCondition.of(condition, nextNodeId);
  }

  // =============== 反向转换辅助方法 ===============

  @Named("processChainIdToString")
  default String processChainIdToString(ProcessChainId id) {
    return id != null ? id.value() : null;
  }

  @Named("processChainNameToString")
  default String processChainNameToString(ProcessChainName name) {
    return name != null ? name.value() : null;
  }

  @Named("processNodeIdToString")
  default String processNodeIdToString(ProcessNodeId id) {
    return id != null ? id.value() : null;
  }

  @Named("processNodeNameToString")
  default String processNodeNameToString(ProcessNodeName name) {
    return name != null ? name.value() : null;
  }

  @Named("versionToString")
  default String versionToString(Version version) {
    return version != null ? version.value() : "1.0.0";
  }

  @Named("listNodesToMap")
  default List<ProcessNode> listNodesToMap(List<ProcessNodeConfig> nodeConfigs) {
    if (nodeConfigs == null || nodeConfigs.isEmpty()) {
      return new ArrayList<>();
    }
    return nodeConfigs.stream()
      .map(this::toProcessNode)
      .collect(Collectors.toList());
  }

  @Named("mapNodesToList")
  default List<ProcessNodeConfig> mapNodesToList(Map<ProcessNodeId, ProcessNode> nodes) {
    if (nodes == null || nodes.isEmpty()) {
      return new java.util.ArrayList<>();
    }
    return nodes.values().stream()
      .map(this::toProcessNodeConfig)
      .collect(java.util.stream.Collectors.toList());
  }

  @Named("nodeTypeToConfigType")
  default ProcessNodeConfig.NodeType nodeTypeToConfigType(NodeType nodeType) {
    if (nodeType == null) {
      return ProcessNodeConfig.NodeType.BUSINESS;
    }
    return switch (nodeType) {
      case BUSINESS -> ProcessNodeConfig.NodeType.BUSINESS;
      case APPROVAL -> ProcessNodeConfig.NodeType.APPROVAL;
      case GATEWAY -> ProcessNodeConfig.NodeType.GATEWAY;
    };
  }

  @Named("ruleChainIdToString")
  default String ruleChainIdToString(com.zornflow.domain.rule.types.RuleChainId ruleChainId) {
    return ruleChainId != null ? ruleChainId.value() : null;
  }

  @Named("gatewayConditionsToConfigConditions")
  default List<GatewayConditionConfig> gatewayConditionsToConfigConditions(List<GatewayCondition> conditions) {
    if (conditions == null || conditions.isEmpty()) {
      return new java.util.ArrayList<>();
    }
    return conditions.stream()
      .map(this::gatewayConditionToConfigCondition)
      .collect(java.util.stream.Collectors.toList());
  }

  default GatewayConditionConfig gatewayConditionToConfigCondition(GatewayCondition gatewayCondition) {
    String condition = gatewayCondition.condition() != null ? gatewayCondition.condition().expression() : null;
    String nextNodeId = gatewayCondition.nextNodeId() != null ? gatewayCondition.nextNodeId().value() : null;
    return new GatewayConditionConfig(condition, nextNodeId);
  }
}
