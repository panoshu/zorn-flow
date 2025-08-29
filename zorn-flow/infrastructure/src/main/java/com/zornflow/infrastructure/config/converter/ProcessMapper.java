package com.zornflow.infrastructure.config.converter;

import com.zornflow.domain.common.types.Version;
import com.zornflow.domain.process.entity.ProcessChain;
import com.zornflow.domain.process.entity.ProcessNode;
import com.zornflow.domain.process.types.ProcessChainId;
import com.zornflow.domain.process.types.ProcessChainName;
import com.zornflow.domain.process.types.ProcessNodeId;
import com.zornflow.domain.process.types.ProcessNodeName;
import com.zornflow.domain.process.valueobject.GatewayCondition;
import com.zornflow.domain.process.valueobject.NodeType;
import com.zornflow.domain.rule.types.Condition;
import com.zornflow.domain.rule.types.RuleChainId;
import com.zornflow.infrastructure.config.model.GatewayConditionConfig;
import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.model.ProcessNodeConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 流程领域映射器
 * <p>
 * 负责流程相关配置模型到领域实体的转换
 * </p>
 *
 * @author Zorn Flow Team
 * @version 1.0
 * @since 2025/8/29
 */
@Mapper(componentModel = "spring")
public interface ProcessMapper {

  // ============================= 流程链转换 =============================

  @Mapping(target = "id", source = "id", qualifiedByName = "stringToProcessChainId")
  @Mapping(target = "name", source = "name", qualifiedByName = "stringToProcessChainName")
  @Mapping(target = "version", source = "version", qualifiedByName = "stringToVersion")
  @Mapping(target = "source", constant = "CONFIG")
  @Mapping(target = "description", source = "description", qualifiedByName = "safeString")
  @Mapping(target = "nodes", source = "nodes")
  ProcessChain toProcessChain(ProcessChainConfig config);

  @Mapping(target = "id", source = "id", qualifiedByName = "stringToProcessNodeId")
  @Mapping(target = "name", source = "name", qualifiedByName = "stringToProcessNodeName")
  @Mapping(target = "type", source = "type", qualifiedByName = "configNodeTypeToNodeType")
  @Mapping(target = "ruleChainId", source = "ruleChain", qualifiedByName = "stringToRuleChainId")
  @Mapping(target = "properties", source = "properties", qualifiedByName = "safeParameterMap")
  @Mapping(target = "conditions", source = "conditions")
  @Mapping(target = "nextNodeId", source = "next", qualifiedByName = "stringToProcessNodeIdOrNull")
  ProcessNode toProcessNode(ProcessNodeConfig config);

  @Mapping(target = "condition", source = "condition", qualifiedByName = "stringToCondition")
  @Mapping(target = "nextNodeId", source = "next", qualifiedByName = "stringToProcessNodeId")
  GatewayCondition toGatewayCondition(GatewayConditionConfig config);

  // ============================= 辅助转换方法 =============================

  @Named("stringToProcessChainId")
  default ProcessChainId stringToProcessChainId(String id) {
    if (!StringUtils.hasText(id)) {
      throw new IllegalArgumentException("ProcessChainId不能为空");
    }
    return ProcessChainId.of(id);
  }

  @Named("stringToProcessNodeId")
  default ProcessNodeId stringToProcessNodeId(String id) {
    if (!StringUtils.hasText(id)) {
      throw new IllegalArgumentException("ProcessNodeId不能为空");
    }
    return ProcessNodeId.of(id);
  }

  @Named("stringToProcessNodeIdOrNull")
  default ProcessNodeId stringToProcessNodeIdOrNull(String id) {
    return StringUtils.hasText(id) ? ProcessNodeId.of(id) : null;
  }

  @Named("stringToRuleChainId")
  default RuleChainId stringToRuleChainId(String id) {
    return StringUtils.hasText(id) ? RuleChainId.of(id) : null;
  }

  @Named("stringToProcessChainName")
  default ProcessChainName stringToProcessChainName(String name) {
    return StringUtils.hasText(name) ? ProcessChainName.of(name) : null;
  }

  @Named("stringToProcessNodeName")
  default ProcessNodeName stringToProcessNodeName(String name) {
    return StringUtils.hasText(name) ? ProcessNodeName.of(name) : null;
  }

  @Named("stringToVersion")
  default Version stringToVersion(String version) {
    return StringUtils.hasText(version) ? Version.of(version) : Version.defaultVersion();
  }

  @Named("stringToCondition")
  default Condition stringToCondition(String condition) {
    return Condition.of(condition);
  }

  @Named("configNodeTypeToNodeType")
  default NodeType configNodeTypeToNodeType(ProcessNodeConfig.NodeType configType) {
    if (configType == null) {
      return NodeType.BUSINESS;
    }
    return switch (configType) {
      case BUSINESS -> NodeType.BUSINESS;
      case APPROVAL -> NodeType.APPROVAL;
      case GATEWAY -> NodeType.GATEWAY;
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
