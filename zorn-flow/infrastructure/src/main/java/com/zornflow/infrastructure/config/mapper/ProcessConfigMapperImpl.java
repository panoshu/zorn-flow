package com.zornflow.infrastructure.config.mapper;

import com.zornflow.domain.process.entity.ProcessChain;
import com.zornflow.domain.process.entity.ProcessNode;
import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.model.ProcessNodeConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Generated;

@Generated(
  value = "org.mapstruct.ap.MappingProcessor",
  date = "2025-08-30T20:11:38+0800",
  comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.1 (Eclipse Adoptium)"
)
public class ProcessConfigMapperImpl implements ProcessConfigMapper {

  @Override
  public ProcessChain toProcessChain(ProcessChainConfig config) {
    if (config == null) {
      return null;
    }

    ProcessChain.ProcessChainBuilder processChain = ProcessChain.builder();

    processChain.id(stringToProcessChainId(config.id()));
    processChain.name(stringToProcessChainName(config.name()));
    processChain.version(stringToVersion(config.version()));
    processChain.description(config.description());
    processChain.nodes(listNodesToMap(config.nodes()));

    processChain.source("YAML");

    return processChain.build();
  }

  @Override
  public ProcessNode toProcessNode(ProcessNodeConfig config) {
    if (config == null) {
      return null;
    }

    ProcessNode.ProcessNodeBuilder processNode = ProcessNode.builder();

    processNode.id(stringToProcessNodeId(config.id()));
    processNode.name(stringToProcessNodeName(config.name()));
    processNode.nextNodeId(stringToProcessNodeId(config.next()));
    processNode.type(configNodeTypeToValueObject(config.type()));
    processNode.ruleChainId(stringToRuleChainId(config.ruleChain()));
    processNode.conditions(configConditionsToGatewayConditions(config.conditions()));
    Map<String, Object> map = config.properties();
    if (map != null) {
      processNode.properties(new LinkedHashMap<String, Object>(map));
    }

    return processNode.build();
  }

  @Override
  public List<ProcessNode> toProcessNodes(List<ProcessNodeConfig> configs) {
    if (configs == null) {
      return null;
    }

    List<ProcessNode> list = new ArrayList<ProcessNode>(configs.size());
    for (ProcessNodeConfig processNodeConfig : configs) {
      list.add(toProcessNode(processNodeConfig));
    }

    return list;
  }

  @Override
  public ProcessChainConfig toProcessChainConfig(ProcessChain processChain) {
    if (processChain == null) {
      return null;
    }

    ProcessChainConfig.ProcessChainConfigBuilder processChainConfig = ProcessChainConfig.builder();

    processChainConfig.id(processChainIdToString(processChain.getId()));
    processChainConfig.name(processChainNameToString(processChain.getName()));
    processChainConfig.version(versionToString(processChain.getVersion()));
    processChainConfig.description(processChain.getDescription());
    processChainConfig.nodes(mapNodesToList(processChain.getNodes()));

    return processChainConfig.build();
  }

  @Override
  public ProcessNodeConfig toProcessNodeConfig(ProcessNode processNode) {
    if (processNode == null) {
      return null;
    }

    ProcessNodeConfig.ProcessNodeConfigBuilder processNodeConfig = ProcessNodeConfig.builder();

    processNodeConfig.id(processNodeIdToString(processNode.getId()));
    processNodeConfig.name(processNodeNameToString(processNode.getName()));
    processNodeConfig.next(processNodeIdToString(processNode.getNextNodeId()));
    processNodeConfig.type(nodeTypeToConfigType(processNode.getType()));
    processNodeConfig.ruleChain(ruleChainIdToString(processNode.getRuleChainId()));
    processNodeConfig.conditions(gatewayConditionsToConfigConditions(processNode.getConditions()));
    Map<String, Object> map = processNode.getProperties();
    if (map != null) {
      processNodeConfig.properties(new LinkedHashMap<String, Object>(map));
    }

    return processNodeConfig.build();
  }

  @Override
  public List<ProcessNodeConfig> toProcessNodeConfigs(List<ProcessNode> processNodes) {
    if (processNodes == null) {
      return null;
    }

    List<ProcessNodeConfig> list = new ArrayList<ProcessNodeConfig>(processNodes.size());
    for (ProcessNode processNode : processNodes) {
      list.add(toProcessNodeConfig(processNode));
    }

    return list;
  }
}
