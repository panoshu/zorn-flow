package com.zornflow.infrastructure.repository.mapper;

import com.zornflow.domain.common.types.Version;
import com.zornflow.domain.rule.entity.Rule;
import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.domain.rule.types.*;
import com.zornflow.domain.rule.valueobject.Handler;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.model.RuleConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(
  componentModel = MappingConstants.ComponentModel.SPRING,
  uses = {CommonTypeMapper.class}
)
public interface RuleConfigMapper {

  @Mapping(target = "id", source = "id", qualifiedByName = "stringToRuleChainId")
  @Mapping(target = "name", source = "name", qualifiedByName = "stringToRuleChainName")
  @Mapping(target = "version", source = "version", qualifiedByName = "stringToVersion")
  @Mapping(target = "rules", source = "rules")
  @Mapping(target = "source", ignore = true)
  RuleChain toDomain(RuleChainConfig dto);

  @Mapping(target = "id", source = "id", qualifiedByName = "stringToRuleId")
  @Mapping(target = "name", source = "name", qualifiedByName = "stringToRuleName")
  @Mapping(target = "priority", source = "priority", qualifiedByName = "integerToPriority")
  @Mapping(target = "condition", source = "condition", qualifiedByName = "stringToCondition")
  @Mapping(target = "handler", source = "handle")
  Rule toDomain(RuleConfig dto);

  @Mapping(target = "id", source = "id", qualifiedByName = "ruleChainIdToString")
  @Mapping(target = "name", source = "name", qualifiedByName = "ruleChainNameToString")
  @Mapping(target = "version", source = "version", qualifiedByName = "versionToString")
  @Mapping(target = "rules", source = "rules")
  @Mapping(target = "createdAt", source = "createdAt")
  @Mapping(target = "updatedAt", source = "updatedAt")
  RuleChainConfig toDto(RuleChain entity);

  @Mapping(target = "id", source = "id", qualifiedByName = "ruleIdToString")
  @Mapping(target = "name", source = "name", qualifiedByName = "ruleNameToString")
  @Mapping(target = "priority", source = "priority", qualifiedByName = "priorityToInteger")
  @Mapping(target = "condition", source = "condition", qualifiedByName = "conditionToString")
  @Mapping(target = "handle", source = "handler")
  @Mapping(target = "sharedRuleId", ignore = true)
  @Mapping(target = "createdAt", source = "createdAt")
  @Mapping(target = "updatedAt", source = "updatedAt")
  RuleConfig toDto(Rule entity);

  @Named("stringToRuleChainId")
  default RuleChainId stringToRuleChainId(String id) {
    return RuleChainId.of(id);
  }

  @Named("ruleChainIdToString")
  default String ruleChainIdToString(RuleChainId id) {
    return id.value();
  }

  @Named("stringToRuleId")
  default RuleId stringToRuleId(String id) {
    return RuleId.of(id);
  }

  @Named("ruleIdToString")
  default String ruleIdToString(RuleId id) {
    return id.value();
  }

  @Named("stringToRuleChainName")
  default RuleChainName stringToRuleChainName(String name) {
    return name != null ? RuleChainName.of(name) : null;
  }

  @Named("ruleChainNameToString")
  default String ruleChainNameToString(RuleChainName name) {
    return name.value();
  }

  @Named("stringToRuleName")
  default RuleName stringToRuleName(String name) {
    return name != null ? RuleName.of(name) : null;
  }

  @Named("ruleNameToString")
  default String ruleNameToString(RuleName name) {
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

  @Named("integerToPriority")
  default Priority integerToPriority(Integer priority) {
    return priority != null ? Priority.of(priority) : null;
  }

  @Named("priorityToInteger")
  default Integer priorityToInteger(Priority priority) {
    return priority.value();
  }

  @Named("stringToCondition")
  default Condition stringToCondition(String condition) {
    return Condition.of(condition);
  }

  @Named("conditionToString")
  default String conditionToString(Condition condition) {
    return condition.expression();
  }

  default Handler toDomain(RuleConfig.HandlerConfig dto) {
    if (dto == null) return null;
    return Handler.of(HandlerType.valueOf(dto.type().name()), dto.handler(), dto.parameters());
  }

  default RuleConfig.HandlerConfig toDto(Handler entity) {
    if (entity == null) return null;
    return new RuleConfig.HandlerConfig(RuleConfig.HandlerConfig.Type.valueOf(entity.type().name()), entity.handler(), entity.parameters());
  }
}
