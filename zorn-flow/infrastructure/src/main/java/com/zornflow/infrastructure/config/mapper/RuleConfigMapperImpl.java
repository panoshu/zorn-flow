package com.zornflow.infrastructure.config.mapper;

import com.zornflow.domain.rule.entity.Rule;
import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.model.RuleConfig;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
  value = "org.mapstruct.ap.MappingProcessor",
  date = "2025-08-30T20:11:38+0800",
  comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.1 (Eclipse Adoptium)"
)
public class RuleConfigMapperImpl implements RuleConfigMapper {

  @Override
  public RuleChain toRuleChain(RuleChainConfig config) {
    if (config == null) {
      return null;
    }

    RuleChain.RuleChainBuilder ruleChain = RuleChain.builder();

    ruleChain.id(stringToRuleChainId(config.id()));
    ruleChain.name(stringToRuleChainName(config.name()));
    ruleChain.version(stringToVersion(config.version()));
    ruleChain.description(config.description());
    ruleChain.rules(toRules(config.rules()));

    ruleChain.source("YAML");

    return ruleChain.build();
  }

  @Override
  public Rule toRule(RuleConfig config) {
    if (config == null) {
      return null;
    }

    Rule.RuleBuilder rule = Rule.builder();

    rule.id(stringToRuleId(config.id()));
    rule.name(stringToRuleName(config.name()));
    rule.priority(intToPriority(config.priority()));
    rule.condition(stringToCondition(config.condition()));
    rule.handlerConfig(configToHandlerConfig(config.handle()));

    return rule.build();
  }

  @Override
  public List<Rule> toRules(List<RuleConfig> configs) {
    if (configs == null) {
      return null;
    }

    List<Rule> list = new ArrayList<Rule>(configs.size());
    for (RuleConfig ruleConfig : configs) {
      list.add(toRule(ruleConfig));
    }

    return list;
  }

  @Override
  public RuleChainConfig toRuleChainConfig(RuleChain ruleChain) {
    if (ruleChain == null) {
      return null;
    }

    RuleChainConfig.RuleChainConfigBuilder ruleChainConfig = RuleChainConfig.builder();

    ruleChainConfig.id(ruleChainIdToString(ruleChain.getId()));
    ruleChainConfig.name(ruleChainNameToString(ruleChain.getName()));
    ruleChainConfig.version(versionToString(ruleChain.getVersion()));
    ruleChainConfig.description(ruleChain.getDescription());
    ruleChainConfig.rules(toRuleConfigs(ruleChain.getRules()));

    return ruleChainConfig.build();
  }

  @Override
  public RuleConfig toRuleConfig(Rule rule) {
    if (rule == null) {
      return null;
    }

    RuleConfig.RuleConfigBuilder ruleConfig = RuleConfig.builder();

    ruleConfig.id(ruleIdToString(rule.getId()));
    ruleConfig.name(ruleNameToString(rule.getName()));
    ruleConfig.priority(priorityToInt(rule.getPriority()));
    ruleConfig.condition(conditionToString(rule.getCondition()));
    ruleConfig.handle(handlerConfigToConfig(rule.getHandlerConfig()));

    return ruleConfig.build();
  }

  @Override
  public List<RuleConfig> toRuleConfigs(List<Rule> rules) {
    if (rules == null) {
      return null;
    }

    List<RuleConfig> list = new ArrayList<RuleConfig>(rules.size());
    for (Rule rule : rules) {
      list.add(toRuleConfig(rule));
    }

    return list;
  }
}
