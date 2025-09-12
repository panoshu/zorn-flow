package com.zornflow.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zornflow.infrastructure.config.model.RecordStatus;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.model.RuleConfig;
import com.zornflow.infrastructure.persistence.jooq.tables.records.ChainRulesRecord;
import com.zornflow.infrastructure.persistence.jooq.tables.records.RuleChainsRecord;
import com.zornflow.infrastructure.persistence.jooq.tables.records.SharedRulesRecord;
import org.jooq.JSONB;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class RulePersistenceMapper {

  protected JsonbMapperHelper jsonbMapperHelper;

  @Autowired
  public void setJsonbMapperHelper(JsonbMapperHelper jsonbMapperHelper) {
    this.jsonbMapperHelper = jsonbMapperHelper;
  }

  @Mapping(target = "status", source = "record.recordStatus")
  @Mapping(target = "rules", source = "rules")
  public abstract RuleChainConfig toDto(RuleChainsRecord record, List<RuleConfig> rules);

  @Mapping(target = "id", source = "id")
  @Mapping(target = "recordStatus", source = "dto.status", qualifiedByName = "mapStatus")
  public abstract void updateRecord(RuleChainConfig dto, @MappingTarget RuleChainsRecord record);

  // --- 单源映射：从 ChainRulesRecord (实例) 到 DTO ---
  @Mapping(target = "id", source = "id")
  @Mapping(target = "sharedRuleId", expression = "java(java.util.Optional.ofNullable(instance.getSharedRuleId()))")
  @Mapping(target = "handle", source = "handlerConfig", qualifiedByName = "jsonbToHandlerConfig")
  @Mapping(target = "status", ignore = true)
  public abstract RuleConfig toDto(ChainRulesRecord instance);

  // --- 单源映射：从 SharedRulesRecord (模板) 到 DTO ---
  @Mapping(target = "id", source = "id")
  @Mapping(target = "handle", source = "handlerConfig", qualifiedByName = "jsonbToHandlerConfig")
  @Mapping(target = "status", source = "recordStatus")
  @Mapping(target = "sharedRuleId", expression = "java(java.util.Optional.empty())")
  public abstract RuleConfig toDto(SharedRulesRecord template);

  @Mapping(target = "id", source = "dto.id")
  @Mapping(target = "ruleChainId", source = "chainId")
  @Mapping(target = "sequence", source = "sequence")
  @Mapping(target = "sharedRuleId", source = "dto.sharedRuleId", qualifiedByName = "optionalToString")
  @Mapping(target = "name", source = "dto.name")
  @Mapping(target = "priority", source = "dto.priority")
  @Mapping(target = "condition", source = "dto.condition")
  @Mapping(target = "handlerConfig", source = "dto.handle", qualifiedByName = "handlerConfigToJsonb")
  public abstract ChainRulesRecord toRecord(RuleConfig dto, String chainId, int sequence);

  // region Named Methods
  @Named("mapStatus")
  protected String mapStatus(String status) {
    return Optional.ofNullable(status).orElse(RecordStatus.ACTIVE.getDbValue());
  }

  @Named("optionalToString")
  protected String optionalToString(Optional<String> optional) {
    return optional.orElse(null);
  }

  @Named("jsonbToHandlerConfig")
  protected RuleConfig.HandlerConfig jsonbToHandlerConfig(JSONB jsonb) {
    return jsonbMapperHelper.fromJsonb(jsonb, new TypeReference<>() {});
  }

  @Named("handlerConfigToJsonb")
  protected JSONB handlerConfigToJsonb(RuleConfig.HandlerConfig handlerConfig) {
    return jsonbMapperHelper.toJsonb(handlerConfig);
  }
  // endregion
}
