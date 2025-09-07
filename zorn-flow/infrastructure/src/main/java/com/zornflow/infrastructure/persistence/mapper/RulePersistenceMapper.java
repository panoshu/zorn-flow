package com.zornflow.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.model.RuleConfig;
import com.zornflow.infrastructure.persistence.jooq.tables.records.ChainRulesRecord;
import com.zornflow.infrastructure.persistence.jooq.tables.records.RuleChainsRecord;
import com.zornflow.infrastructure.persistence.jooq.tables.records.SharedRulesRecord;
import org.jooq.JSONB;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = JsonbMapperHelper.class)
public interface RulePersistenceMapper {

  @Mapping(target = "status", source = "record.recordStatus")
  @Mapping(target = "rules", source = "rules")
  RuleChainConfig toDto(RuleChainsRecord record, List<RuleConfig> rules);

  @Mapping(target = "id", source = "id")
  @Mapping(target = "recordStatus", expression = "java(java.util.Optional.ofNullable(dto.status()).orElse(com.zornflow.infrastructure.config.model.RecordStatus.ACTIVE.getDbValue()))")
  void updateRecord(RuleChainConfig dto, @MappingTarget RuleChainsRecord record);

  @Mapping(target = "id", source = "instance.id")
  @Mapping(target = "sharedRuleId", expression = "java(java.util.Optional.of(template.getId()))")
  @Mapping(target = "name", expression = "java(java.util.Optional.ofNullable(instance.getName()).orElse(template.getName()))")
  @Mapping(target = "priority", expression = "java(java.util.Optional.ofNullable(instance.getPriority()).orElse(template.getPriority()))")
  @Mapping(target = "condition", expression = "java(java.util.Optional.ofNullable(instance.getCondition()).orElse(template.getCondition()))")
  @Mapping(target = "handle", expression = "java(mergeHandlerConfig(instance.getHandlerConfig(), template, helper))")
  @Mapping(target = "status", source = "template.recordStatus")
  @Mapping(target = "version", source = "instance.version")
  @Mapping(target = "createdAt", source = "instance.createdAt")
  @Mapping(target = "updatedAt", source = "instance.updatedAt")
  RuleConfig toDto(SharedRulesRecord template, ChainRulesRecord instance, @Context JsonbMapperHelper helper);

  @Mapping(target = "id", source = "id")
  @Mapping(target = "sharedRuleId", expression = "java(java.util.Optional.empty())")
  @Mapping(target = "handle", source = "handlerConfig", qualifiedByName = "jsonbToHandlerConfig")
  @Mapping(target = "status", ignore = true)
  RuleConfig toDto(ChainRulesRecord instance, @Context JsonbMapperHelper helper);

  @Mapping(target = "id", source = "dto.id")
  @Mapping(target = "ruleChainId", source = "chainId")
  @Mapping(target = "sequence", source = "sequence")
  @Mapping(target = "sharedRuleId", expression = "java(dto.sharedRuleId().orElse(null))")
  @Mapping(target = "name", source = "dto.name")
  @Mapping(target = "priority", source = "dto.priority")
  @Mapping(target = "condition", source = "dto.condition")
  @Mapping(target = "handlerConfig", expression = "java(helper.toJsonb(dto.handle()))")
  ChainRulesRecord toRecord(RuleConfig dto, String chainId, int sequence, @Context JsonbMapperHelper helper);

  @Named("jsonbToHandlerConfig")
  default RuleConfig.HandlerConfig jsonbToHandlerConfig(JSONB jsonb, @Context JsonbMapperHelper helper) {
    return helper.fromJsonb(jsonb, new TypeReference<>() {});
  }

  default RuleConfig.HandlerConfig mergeHandlerConfig(JSONB instanceJsonb, SharedRulesRecord template, @Context JsonbMapperHelper helper) {
    RuleConfig.HandlerConfig instanceHandler = jsonbToHandlerConfig(instanceJsonb, helper);
    if (instanceHandler != null) {
      return instanceHandler;
    }
    return helper.fromJsonb(template.getHandlerConfig(), new TypeReference<>() {});
  }
}
