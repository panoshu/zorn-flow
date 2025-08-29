package com.zornflow.infrastructure.converter;

import com.zornflow.domain.common.types.Version;
import com.zornflow.domain.rule.entity.Rule;
import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.domain.rule.types.*;
import com.zornflow.domain.rule.valueobject.HandlerConfig;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.model.RuleConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 规则领域映射器
 * <p>
 * 负责规则相关配置模型到领域实体的转换
 * </p>
 *
 * @author Zorn Flow Team
 * @version 1.0
 * @since 2025/8/29
 */
@Mapper(componentModel = "spring")
public interface RuleMapper {

    // ============================= 规则链转换 =============================

    @Mapping(target = "id", source = "id", qualifiedByName = "stringToRuleChainId")
    @Mapping(target = "name", source = "name", qualifiedByName = "stringToRuleChainName")
    @Mapping(target = "version", source = "version", qualifiedByName = "stringToVersion")
    @Mapping(target = "source", constant = "CONFIG")
    @Mapping(target = "description", source = "description", qualifiedByName = "safeString")
    @Mapping(target = "rules", source = "rules")
    RuleChain toRuleChain(RuleChainConfig config);

    @Mapping(target = "id", source = "id", qualifiedByName = "stringToRuleId")
    @Mapping(target = "name", source = "name", qualifiedByName = "stringToRuleName")
    @Mapping(target = "priority", source = "priority", qualifiedByName = "integerToPriority")
    @Mapping(target = "condition", source = "condition", qualifiedByName = "stringToCondition")
    @Mapping(target = "handlerConfig", source = "handle")
    Rule toRule(RuleConfig config);

    @Mapping(target = "type", source = "type", qualifiedByName = "configHandlerTypeToHandlerType")
    @Mapping(target = "handler", source = "handler")
    @Mapping(target = "parameters", source = "parameters", qualifiedByName = "safeParameterMap")
    HandlerConfig toHandlerConfig(RuleConfig.Handler handle);

    // ============================= 辅助转换方法 =============================

    @Named("stringToRuleChainId")
    default RuleChainId stringToRuleChainId(String id) {
        return StringUtils.hasText(id) ? RuleChainId.of(id) : null;
    }

    @Named("stringToRuleId")
    default RuleId stringToRuleId(String id) {
        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("RuleId不能为空");
        }
        return RuleId.of(id);
    }

    @Named("stringToRuleChainName")
    default RuleChainName stringToRuleChainName(String name) {
        return StringUtils.hasText(name) ? RuleChainName.of(name) : null;
    }

    @Named("stringToRuleName")
    default RuleName stringToRuleName(String name) {
        return StringUtils.hasText(name) ? RuleName.of(name) : null;
    }

    @Named("stringToVersion")
    default Version stringToVersion(String version) {
        return StringUtils.hasText(version) ? Version.of(version) : Version.defaultVersion();
    }

    @Named("integerToPriority")
    default Priority integerToPriority(Integer priority) {
        return priority != null ? Priority.of(priority) : Priority.defaultPriority();
    }

    @Named("stringToCondition")
    default Condition stringToCondition(String condition) {
        return Condition.of(condition);
    }

    @Named("configHandlerTypeToHandlerType")
    default HandlerType configHandlerTypeToHandlerType(RuleConfig.Handler.Type configType) {
        if (configType == null) {
            return HandlerType.CLASS;
        }
        return switch (configType) {
            case CLASS -> HandlerType.CLASS;
            case SCRIPT -> HandlerType.SCRIPT;
            case JAR -> HandlerType.JAR;
        };
    }

    @Named("safeParameterMap")
    default Map<String, Object> safeParameterMap(Map<String, Object> parameters) {
        return parameters != null ? Map.copyOf(parameters) : Map.of();
    }

    @Named("safeString")
    default String safeString(String str) {
        return str != null ? str : "";
    }
}
