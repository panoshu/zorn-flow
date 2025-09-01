package com.zornflow.infrastructure.repository.mapper;

import com.zornflow.domain.common.types.Version;
import com.zornflow.domain.process.entity.ProcessChain;
import com.zornflow.domain.process.entity.ProcessNode;
import com.zornflow.domain.process.types.ProcessChainId;
import com.zornflow.domain.process.types.ProcessChainName;
import com.zornflow.domain.process.types.ProcessNodeId;
import com.zornflow.domain.process.types.ProcessNodeName;
import com.zornflow.domain.process.valueobject.GatewayCondition;
import com.zornflow.domain.process.valueobject.NodeType;
import com.zornflow.domain.rule.types.Condition;
import com.zornflow.domain.rule.types.RuleChainId;
import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.model.ProcessNodeConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProcessConfigMapper {

  @Mapping(target = "id", source = "id", qualifiedByName = "stringToProcessChainId")
  @Mapping(target = "name", source = "name", qualifiedByName = "stringToProcessChainName")
  @Mapping(target = "version", source = "version", qualifiedByName = "stringToVersion")
  @Mapping(target = "nodes", source = "nodes")
  @Mapping(target = "source", ignore = true)
  ProcessChain toDomain(ProcessChainConfig dto);

  @Mapping(target = "id", source = "id", qualifiedByName = "stringToProcessNodeId")
  @Mapping(target = "name", source = "name", qualifiedByName = "stringToProcessNodeName")
  @Mapping(target = "nextNodeId", source = "next", qualifiedByName = "stringToProcessNodeId")
  @Mapping(target = "ruleChainId", source = "ruleChain", qualifiedByName = "stringToRuleChainId")
  ProcessNode toDomain(ProcessNodeConfig dto);

  @Mapping(target = "id", source = "id", qualifiedByName = "processChainIdToString")
  @Mapping(target = "name", source = "name", qualifiedByName = "processChainNameToString")
  @Mapping(target = "version", source = "version", qualifiedByName = "versionToString")
  @Mapping(target = "nodes", source = "allNodes")
  ProcessChainConfig toDto(ProcessChain entity);

  @Mapping(target = "id", source = "id", qualifiedByName = "processNodeIdToString")
  @Mapping(target = "name", source = "name", qualifiedByName = "processNodeNameToString")
  @Mapping(target = "next", source = "nextNodeId", qualifiedByName = "processNodeIdToString")
  @Mapping(target = "ruleChain", source = "ruleChainId", qualifiedByName = "ruleChainIdToString")
  @Mapping(target = "sharedNodeId", ignore = true)
  ProcessNodeConfig toDto(ProcessNode entity);

  @Named("stringToProcessChainId")
  default ProcessChainId stringToProcessChainId(String id) {
    return ProcessChainId.of(id);
  }

  @Named("processChainIdToString")
  default String processChainIdToString(ProcessChainId id) {
    return id.value();
  }

  @Named("stringToProcessNodeId")
  default ProcessNodeId stringToProcessNodeId(String id) {
    return id != null ? ProcessNodeId.of(id) : null;
  }

  @Named("processNodeIdToString")
  default String processNodeIdToString(ProcessNodeId id) {
    return id != null ? id.value() : null;
  }

  @Named("stringToProcessChainName")
  default ProcessChainName stringToProcessChainName(String name) {
    return name != null ? ProcessChainName.of(name) : null;
  }

  @Named("processChainNameToString")
  default String processChainNameToString(ProcessChainName name) {
    return name.value();
  }

  @Named("stringToProcessNodeName")
  default ProcessNodeName stringToProcessNodeName(String name) {
    return name != null ? ProcessNodeName.of(name) : null;
  }

  @Named("processNodeNameToString")
  default String processNodeNameToString(ProcessNodeName name) {
    return name.value();
  }

  @Named("stringToVersion")
  default Version stringToVersion(String version) {
    return Version.of(version);
  }

  @Named("versionToString")
  default String versionToString(Version version) {
    return version.value();
  }

  @Named("stringToRuleChainId")
  default RuleChainId stringToRuleChainId(String id) {
    return id != null ? RuleChainId.of(id) : null;
  }

  @Named("ruleChainIdToString")
  default String ruleChainIdToString(RuleChainId id) {
    return id != null ? id.value() : null;
  }

  default NodeType toDomain(ProcessNodeConfig.NodeType type) {
    return type != null ? NodeType.valueOf(type.name()) : null;
  }

  default ProcessNodeConfig.NodeType toDto(NodeType type) {
    return type != null ? ProcessNodeConfig.NodeType.valueOf(type.name()) : null;
  }

  default List<GatewayCondition> toDomain(List<ProcessNodeConfig.GatewayConditionConfig> dtos) {
    if (dtos == null) return List.of();
    return dtos.stream()
      .map(dto -> new GatewayCondition(Condition.of(dto.condition()), ProcessNodeId.of(dto.next())))
      .collect(Collectors.toList());
  }

  default List<ProcessNodeConfig.GatewayConditionConfig> toDto(List<GatewayCondition> entities) {
    if (entities == null) return List.of();
    return entities.stream()
      .map(entity -> new ProcessNodeConfig.GatewayConditionConfig(entity.condition().expression(), entity.nextNodeId().value()))
      .collect(Collectors.toList());
  }
}
