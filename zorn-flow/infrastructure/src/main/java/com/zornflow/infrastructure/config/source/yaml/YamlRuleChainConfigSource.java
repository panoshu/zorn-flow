package com.zornflow.infrastructure.config.source.yaml;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.model.RuleConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/31 4:00
 */

@Slf4j
@Component
@RequiredArgsConstructor
public non-sealed class YamlRuleChainConfigSource extends AbstractYamlConfigSource<RuleChainConfig, RuleConfig>{

  private final YamlConfigProperties yamlConfigProperties;

  @Override
  protected String getItemName() {
    return "Rules";
  }

  @Override
  protected String getSharedItemsPath() {
    return yamlConfigProperties.getSharedRulesPath();
  }

  @Override
  protected String getChainsPath() {
    return yamlConfigProperties.getRuleChainsPath();
  }

  @Override
  protected TypeReference<Map<String, RuleConfig>> getSharedItemTypeReference() {
    return new TypeReference<>() {};
  }

  @Override
  protected TypeReference<Map<String, RuleChainConfig>> getChainTypeReference() {
    return new TypeReference<>() {};
  }

  @Override
  protected RuleChainConfig mergeChain(RuleChainConfig chain, Map<String, RuleConfig> sharedRules) {
    List<RuleConfig> mergedRules = chain.rules().stream()
      .map(localRule -> Optional.ofNullable(sharedRules.get(localRule.id()))
        .map(localRule::mergeWithDefaults)
        .orElse(localRule)
      ).toList();

    return new RuleChainConfig(chain.id(), chain.name(), chain.version(), chain.description(),
      mergedRules, null, null);
  }
}
