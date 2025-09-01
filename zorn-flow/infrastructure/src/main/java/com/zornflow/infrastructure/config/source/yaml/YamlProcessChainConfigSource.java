package com.zornflow.infrastructure.config.source.yaml;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.model.ProcessNodeConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/31 18:11
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public non-sealed class YamlProcessChainConfigSource extends AbstractYamlConfigSource<ProcessChainConfig, ProcessNodeConfig> {

  private final YamlConfigProperties yamlConfigProperties;

  @Override
  protected String getItemName() {
    return "Nodes";
  }

  @Override
  protected String getSharedItemsPath() {
    return yamlConfigProperties.getSharedNodesPath();
  }

  @Override
  protected String getChainsPath() {
    return yamlConfigProperties.getProcessChainsPath();
  }

  @Override
  protected TypeReference<Map<String, ProcessNodeConfig>> getSharedItemTypeReference() {
    return new TypeReference<>() {};
  }

  @Override
  protected TypeReference<Map<String, ProcessChainConfig>> getChainTypeReference() {
    return new TypeReference<>() {};
  }

  @Override
  protected ProcessChainConfig mergeChain(ProcessChainConfig chain, Map<String, ProcessNodeConfig> sharedNodes) {
    List<ProcessNodeConfig> mergedNodes = chain.nodes().stream()
      .map(localNode -> Optional.ofNullable(sharedNodes.get(localNode.id()))
        .map(localNode::mergeWithDefaults)
        .orElse(localNode)
      ).toList();

    return new ProcessChainConfig(chain.id(), chain.name(), chain.version(), chain.description(), mergedNodes);
  }
}
