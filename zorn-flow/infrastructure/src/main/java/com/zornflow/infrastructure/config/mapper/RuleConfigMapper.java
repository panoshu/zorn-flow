package com.zornflow.infrastructure.config.mapper;

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
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 规则配置映射器
 * 将基础设施层配置DTO转换为领域层实体
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29 11:35
 */
@Mapper
public interface RuleConfigMapper {

    RuleConfigMapper INSTANCE = Mappers.getMapper(RuleConfigMapper.class);

    /**
     * 转换规则链配置为规则链实体
     */
    @Mapping(target = "id", source = "id", qualifiedByName = "stringToRuleChainId")
    @Mapping(target = "name", source = "name", qualifiedByName = "stringToRuleChainName")
    @Mapping(target = "version", source = "version", qualifiedByName = "stringToVersion")
    @Mapping(target = "source", constant = "YAML")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "rules", source = "rules")
    RuleChain toRuleChain(RuleChainConfig config);

    /**
     * 转换规则配置为规则实体
     */
    @Mapping(target = "id", source = "id", qualifiedByName = "stringToRuleId")
    @Mapping(target = "name", source = "name", qualifiedByName = "stringToRuleName")
    @Mapping(target = "priority", source = "priority", qualifiedByName = "intToPriority")
    @Mapping(target = "condition", source = "condition", qualifiedByName = "stringToCondition")
    @Mapping(target = "handlerConfig", source = "handle", qualifiedByName = "configToHandlerConfig")
    Rule toRule(RuleConfig config);

    /**
     * 批量转换规则
     */
    List<Rule> toRules(List<RuleConfig> configs);

    // =============== 反向转换方法（领域实体到配置DTO） ===============

    /**
     * 转换规则链实体为规则链配置
     */
    @Mapping(target = "id", source = "id", qualifiedByName = "ruleChainIdToString")
    @Mapping(target = "name", source = "name", qualifiedByName = "ruleChainNameToString")
    @Mapping(target = "version", source = "version", qualifiedByName = "versionToString")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "rules", source = "rules")
    RuleChainConfig toRuleChainConfig(RuleChain ruleChain);

    /**
     * 转换规则实体为规则配置
     */
    @Mapping(target = "id", source = "id", qualifiedByName = "ruleIdToString")
    @Mapping(target = "name", source = "name", qualifiedByName = "ruleNameToString")
    @Mapping(target = "priority", source = "priority", qualifiedByName = "priorityToInt")
    @Mapping(target = "condition", source = "condition", qualifiedByName = "conditionToString")
    @Mapping(target = "handle", source = "handlerConfig", qualifiedByName = "handlerConfigToConfig")
    RuleConfig toRuleConfig(Rule rule);

    /**
     * 批量转换规则（实体到配置）
     */
    List<RuleConfig> toRuleConfigs(List<Rule> rules);

    // 类型转换方法

    @Named("stringToRuleChainId")
    default RuleChainId stringToRuleChainId(String id) {
        return id != null ? RuleChainId.of(id) : null;
    }

    @Named("stringToRuleChainName")
    default RuleChainName stringToRuleChainName(String name) {
        return name != null ? RuleChainName.of(name) : null;
    }

    @Named("stringToRuleId")
    default RuleId stringToRuleId(String id) {
        return id != null ? RuleId.of(id) : null;
    }

    @Named("stringToRuleName")
    default RuleName stringToRuleName(String name) {
        return name != null ? RuleName.of(name) : null;
    }

    @Named("intToPriority")
    default Priority intToPriority(Integer priority) {
        return priority != null ? Priority.of(priority) : Priority.of(100);
    }

    @Named("stringToVersion")
    default Version stringToVersion(String version) {
        if (version == null || version.isBlank()) {
            return Version.of("1.0.0");
        }
        // 如果版本号不符合 x.y.z 格式，自动补充
        String[] parts = version.split("\\.");
        if (parts.length == 2) {
            return Version.of(version + ".0");
        } else if (parts.length == 1) {
            return Version.of(version + ".0.0");
        }
        return Version.of(version);
    }

    @Named("stringToCondition")
    default Condition stringToCondition(String condition) {
        return condition != null ? Condition.of(condition) : null;
    }

    @Named("configToHandlerConfig")
    default HandlerConfig configToHandlerConfig(RuleConfig.Handler handle) {
        if (handle == null) {
            return null;
        }

        HandlerType type = switch (handle.type()) {
            case CLASS -> HandlerType.CLASS;
            case SCRIPT -> HandlerType.SCRIPT;
            case JAR -> HandlerType.JAR;
        };

        return HandlerConfig.of(type, handle.handler(), handle.parameters());
    }

    // =============== 反向转换辅助方法 ===============

    @Named("ruleChainIdToString")
    default String ruleChainIdToString(RuleChainId id) {
        return id != null ? id.value() : null;
    }

    @Named("ruleChainNameToString")
    default String ruleChainNameToString(RuleChainName name) {
        return name != null ? name.value() : null;
    }

    @Named("ruleIdToString")
    default String ruleIdToString(RuleId id) {
        return id != null ? id.value() : null;
    }

    @Named("ruleNameToString")
    default String ruleNameToString(RuleName name) {
        return name != null ? name.value() : null;
    }

    @Named("priorityToInt")
    default Integer priorityToInt(Priority priority) {
        return priority != null ? priority.value() : 100;
    }

    @Named("versionToString")
    default String versionToString(Version version) {
        return version != null ? version.value() : "1.0.0";
    }

    @Named("conditionToString")
    default String conditionToString(Condition condition) {
        return condition != null ? condition.expression() : null;
    }

    @Named("handlerConfigToConfig")
    default RuleConfig.Handler handlerConfigToConfig(HandlerConfig handlerConfig) {
        if (handlerConfig == null) {
            return null;
        }

        RuleConfig.Handler.Type type = switch (handlerConfig.type()) {
            case CLASS -> RuleConfig.Handler.Type.CLASS;
            case SCRIPT -> RuleConfig.Handler.Type.SCRIPT;
            case JAR -> RuleConfig.Handler.Type.JAR;
        };

        return new RuleConfig.Handler(type, handlerConfig.handler(), handlerConfig.parameters());
    }
}
