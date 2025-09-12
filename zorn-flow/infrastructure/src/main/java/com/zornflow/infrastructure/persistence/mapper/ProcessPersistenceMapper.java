package com.zornflow.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.model.ProcessNodeConfig;
import com.zornflow.infrastructure.config.model.ProcessNodeConfig.GatewayConditionConfig;
import com.zornflow.infrastructure.config.model.RecordStatus;
import com.zornflow.infrastructure.persistence.jooq.tables.records.ChainNodesRecord;
import com.zornflow.infrastructure.persistence.jooq.tables.records.ProcessChainsRecord;
import com.zornflow.infrastructure.persistence.jooq.tables.records.SharedNodesRecord;
import org.jooq.JSONB;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ProcessPersistenceMapper {

  protected JsonbMapperHelper jsonbMapperHelper;

  @Autowired
  public void setJsonbMapperHelper(JsonbMapperHelper jsonbMapperHelper) {
    this.jsonbMapperHelper = jsonbMapperHelper;
  }

  @Mapping(target = "status", source = "record.recordStatus")
  @Mapping(target = "nodes", source = "nodes")
  public abstract ProcessChainConfig toDto(ProcessChainsRecord record, List<ProcessNodeConfig> nodes);

  @Mapping(target = "id", source = "id")
  @Mapping(target = "recordStatus", source = "dto.status", qualifiedByName = "mapStatus")
  public abstract void updateRecord(ProcessChainConfig dto, @MappingTarget ProcessChainsRecord record);

  // --- 单源映射：从 ChainNodesRecord (实例) 到 DTO ---
  @Mapping(target = "id", source = "id")
  @Mapping(target = "next", source = "nextNodeId")
  @Mapping(target = "ruleChain", source = "ruleChainId")
  @Mapping(target = "sharedNodeId", expression = "java(java.util.Optional.ofNullable(instance.getSharedNodeId()))")
  @Mapping(target = "type", source = "nodeType", qualifiedByName = "stringToNodeType")
  @Mapping(target = "conditions", source = "conditions", qualifiedByName = "jsonbToConditions")
  @Mapping(target = "properties", source = "properties", qualifiedByName = "jsonbToProperties")
  @Mapping(target = "status", ignore = true)
  public abstract ProcessNodeConfig toDto(ChainNodesRecord instance);

  // --- 单源映射：从 SharedNodesRecord (模板) 到 DTO ---
  @Mapping(target = "id", source = "id")
  @Mapping(target = "type", source = "nodeType", qualifiedByName = "stringToNodeType")
  @Mapping(target = "conditions", source = "conditions", qualifiedByName = "jsonbToConditions")
  @Mapping(target = "properties", source = "properties", qualifiedByName = "jsonbToProperties")
  @Mapping(target = "status", source = "recordStatus")
  @Mapping(target = "ruleChain", source = "ruleChainId")
  @Mapping(target = "sharedNodeId", expression = "java(java.util.Optional.empty())")
  @Mapping(target = "next", ignore = true)
  public abstract ProcessNodeConfig toDto(SharedNodesRecord template);

  @Mapping(target = "id", source = "dto.id")
  @Mapping(target = "processChainId", source = "chainId")
  @Mapping(target = "sequence", source = "sequence")
  @Mapping(target = "sharedNodeId", source = "dto.sharedNodeId", qualifiedByName = "optionalToString")
  @Mapping(target = "name", source = "dto.name")
  @Mapping(target = "nextNodeId", source = "dto.next")
  @Mapping(target = "ruleChainId", source = "dto.ruleChain")
  @Mapping(target = "nodeType", source = "dto.type", qualifiedByName = "nodeTypeToString")
  @Mapping(target = "conditions", source = "dto.conditions", qualifiedByName = "conditionsToJsonb")
  @Mapping(target = "properties", source = "dto.properties", qualifiedByName = "propertiesToJsonb")
  public abstract ChainNodesRecord toRecord(ProcessNodeConfig dto, String chainId, int sequence);

  // region Named Methods
  @Named("mapStatus")
  protected String mapStatus(String status) {
    return Optional.ofNullable(status).orElse(RecordStatus.ACTIVE.getDbValue());
  }

  @Named("optionalToString")
  protected String optionalToString(Optional<String> optional) {
    return optional.orElse(null);
  }

  @Named("stringToNodeType")
  protected ProcessNodeConfig.NodeType stringToNodeType(String nodeType) {
    return nodeType != null ? ProcessNodeConfig.NodeType.valueOf(nodeType) : null;
  }

  @Named("nodeTypeToString")
  protected String nodeTypeToString(ProcessNodeConfig.NodeType nodeType) {
    return nodeType != null ? nodeType.name() : null;
  }

  @Named("jsonbToConditions")
  protected List<GatewayConditionConfig> jsonbToConditions(JSONB jsonb) {
    return jsonbMapperHelper.fromJsonb(jsonb, new TypeReference<>() {});
  }

  @Named("conditionsToJsonb")
  protected JSONB conditionsToJsonb(List<GatewayConditionConfig> conditions) {
    return jsonbMapperHelper.toJsonb(conditions);
  }

  @Named("jsonbToProperties")
  protected Map<String, Object> jsonbToProperties(JSONB jsonb) {
    return jsonbMapperHelper.fromJsonb(jsonb, new TypeReference<>() {});
  }

  @Named("propertiesToJsonb")
  protected JSONB propertiesToJsonb(Map<String, Object> properties) {
    return jsonbMapperHelper.toJsonb(properties);
  }
  // endregion
}
