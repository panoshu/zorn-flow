package com.zornflow.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.model.ProcessNodeConfig;
import com.zornflow.infrastructure.config.model.ProcessNodeConfig.GatewayConditionConfig;
import com.zornflow.infrastructure.persistence.jooq.tables.records.ChainNodesRecord;
import com.zornflow.infrastructure.persistence.jooq.tables.records.ProcessChainsRecord;
import com.zornflow.infrastructure.persistence.jooq.tables.records.SharedNodesRecord;
import org.jooq.JSONB;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = JsonbMapperHelper.class)
public interface ProcessPersistenceMapper {

  @Mapping(target = "status", source = "record.recordStatus")
  @Mapping(target = "nodes", source = "nodes")
  ProcessChainConfig toDto(ProcessChainsRecord record, List<ProcessNodeConfig> nodes);

  @Mapping(target = "id", source = "id")
  @Mapping(target = "recordStatus", expression = "java(java.util.Optional.ofNullable(dto.status()).orElse(com.zornflow.infrastructure.config.model.RecordStatus.ACTIVE.getDbValue()))")
  void updateRecord(ProcessChainConfig dto, @MappingTarget ProcessChainsRecord record);

  @Mapping(target = "id", source = "instance.id")
  @Mapping(target = "next", source = "instance.nextNodeId")
  @Mapping(target = "sharedNodeId", expression = "java(java.util.Optional.of(template.getId()))")
  @Mapping(target = "name", expression = "java(java.util.Optional.ofNullable(instance.getName()).orElse(template.getName()))")
  @Mapping(target = "type", expression = "java(mergeNodeType(instance.getNodeType(), template))")
  @Mapping(target = "ruleChain", expression = "java(mergeRuleChainId(instance.getRuleChainId(), template))")
  @Mapping(target = "conditions", expression = "java(mergeConditions(instance.getConditions(), template, helper))")
  @Mapping(target = "properties", expression = "java(mergeProperties(instance.getProperties(), template, helper))")
  @Mapping(target = "status", source = "template.recordStatus")
  @Mapping(target = "version", source = "instance.version")
  @Mapping(target = "createdAt", source = "instance.createdAt")
  @Mapping(target = "updatedAt", source = "instance.updatedAt")
  ProcessNodeConfig toDto(SharedNodesRecord template, ChainNodesRecord instance, @Context JsonbMapperHelper helper);

  @Mapping(target = "id", source = "id")
  @Mapping(target = "next", source = "nextNodeId")
  @Mapping(target = "ruleChain", source = "ruleChainId")
  @Mapping(target = "sharedNodeId", expression = "java(java.util.Optional.empty())")
  @Mapping(target = "type", source = "nodeType", qualifiedByName = "stringToNodeType")
  @Mapping(target = "conditions", source = "conditions", qualifiedByName = "jsonbToConditions")
  @Mapping(target = "properties", source = "properties", qualifiedByName = "jsonbToProperties")
  @Mapping(target = "status", ignore = true)
  ProcessNodeConfig toDto(ChainNodesRecord instance, @Context JsonbMapperHelper helper);

  @Mapping(target = "id", source = "dto.id")
  @Mapping(target = "processChainId", source = "chainId")
  @Mapping(target = "sequence", source = "sequence")
  @Mapping(target = "sharedNodeId", expression = "java(dto.sharedNodeId().orElse(null))")
  @Mapping(target = "name", source = "dto.name")
  @Mapping(target = "nextNodeId", source = "dto.next")
  @Mapping(target = "ruleChainId", source = "dto.ruleChain")
  @Mapping(target = "nodeType", expression = "java(dto.type() != null ? dto.type().name() : null)")
  @Mapping(target = "conditions", expression = "java(helper.toJsonb(dto.conditions()))")
  @Mapping(target = "properties", expression = "java(helper.toJsonb(dto.properties()))")
  ChainNodesRecord toRecord(ProcessNodeConfig dto, String chainId, int sequence, @Context JsonbMapperHelper helper);

  @Named("stringToNodeType")
  default ProcessNodeConfig.NodeType stringToNodeType(String nodeType) {
    return nodeType != null ? ProcessNodeConfig.NodeType.valueOf(nodeType) : null;
  }

  default ProcessNodeConfig.NodeType mergeNodeType(String instanceType, SharedNodesRecord template) {
    return ProcessNodeConfig.NodeType.valueOf(Optional.ofNullable(instanceType).orElse(template.getNodeType()));
  }

  default String mergeRuleChainId(String instanceRuleChainId, SharedNodesRecord template) {
    return Optional.ofNullable(instanceRuleChainId).orElse(template.getRuleChainId());
  }

  @Named("jsonbToConditions")
  default List<GatewayConditionConfig> jsonbToConditions(JSONB jsonb, @Context JsonbMapperHelper helper) {
    return helper.fromJsonb(jsonb, new TypeReference<>() {
    });
  }

  default List<GatewayConditionConfig> mergeConditions(JSONB instanceJsonb, SharedNodesRecord template, @Context JsonbMapperHelper helper) {
    List<GatewayConditionConfig> instanceConditions = jsonbToConditions(instanceJsonb, helper);
    return instanceConditions != null && !instanceConditions.isEmpty() ? instanceConditions : helper.fromJsonb(template.getConditions(), new TypeReference<>() {
    });
  }

  @Named("jsonbToProperties")
  default Map<String, Object> jsonbToProperties(JSONB jsonb, @Context JsonbMapperHelper helper) {
    return helper.fromJsonb(jsonb, new TypeReference<>() {
    });
  }

  default Map<String, Object> mergeProperties(JSONB instanceJsonb, SharedNodesRecord template, @Context JsonbMapperHelper helper) {
    Map<String, Object> instanceProperties = jsonbToProperties(instanceJsonb, helper);
    return instanceProperties != null ? instanceProperties : helper.fromJsonb(template.getProperties(), new TypeReference<>() {
    });
  }
}
