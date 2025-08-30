package com.zornflow.infrastructure.config.dataaccess.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zornflow.infrastructure.config.dataaccess.database.dto.RuleChainDto;
import com.zornflow.infrastructure.config.model.*;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Record;

import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;

/**
 * 数据库配置源辅助类
 * 处理数据转换和复杂查询逻辑
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
@Slf4j
public class DatabaseConfigSourceHelper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // =============== 读取方法 ===============

    /**
     * 加载规则链的规则列表，支持全局引用和私有规则
     */
    public List<RuleConfig> loadRuleChainRules(DSLContext dsl, String ruleChainId, Map<String, RuleConfig> globalRules, String ruleChainRulesTable) {
        return dsl.select()
            .from(table(ruleChainRulesTable))
            .where(field("rule_chain_id").eq(ruleChainId))
            .orderBy(field("rule_order"))
            .fetch()
            .stream()
            .map(record -> convertToRuleConfigFromChain(record, globalRules))
            .collect(Collectors.toList());
    }

    /**
     * 加载流程链的节点列表，支持全局引用和私有节点
     */
    public List<ProcessNodeConfig> loadProcessChainNodes(DSLContext dsl, String processChainId, Map<String, ProcessNodeConfig> globalNodes,
                                                        String processChainNodesTable, String gatewayConditionsTable) {
        List<ProcessNodeConfig> nodes = dsl.select()
            .from(table(processChainNodesTable))
            .where(field("process_chain_id").eq(processChainId))
            .orderBy(field("node_order"))
            .fetch()
            .stream()
            .map(record -> convertToProcessNodeConfigFromChain(record, globalNodes))
            .collect(Collectors.toList());

        // 为Gateway节点加载条件信息
        for (int i = 0; i < nodes.size(); i++) {
            ProcessNodeConfig node = nodes.get(i);
            if (node.type() == ProcessNodeConfig.NodeType.GATEWAY) {
                List<GatewayConditionConfig> conditions = loadGatewayConditions(dsl, node.id(), processChainId, gatewayConditionsTable);
                nodes.set(i, ProcessNodeConfig.builder()
                    .id(node.id())
                    .name(node.name())
                    .next(node.next())
                    .type(node.type())
                    .ruleChain(node.ruleChain())
                    .conditions(conditions)
                    .properties(node.properties())
                    .build());
            }
        }

        return nodes;
    }

    /**
     * 加载网关条件配置
     */
    private List<GatewayConditionConfig> loadGatewayConditions(DSLContext dsl, String nodeId, String processChainId, String gatewayConditionsTable) {
        return dsl.select()
            .from(table(gatewayConditionsTable))
            .where(field("node_id").eq(nodeId))
            .and(field("process_chain_id").eq(processChainId))
            .orderBy(field("condition_order"))
            .fetch()
            .stream()
            .map(record -> new GatewayConditionConfig(
                record.get("condition_expr", String.class),
                record.get("next_node_id", String.class)
            ))
            .collect(Collectors.toList());
    }

    // =============== 数据转换方法 ===============

    /**
     * 将数据库记录转换为RuleConfig（从规则链-规则关联表）
     */
    private RuleConfig convertToRuleConfigFromChain(Record record, Map<String, RuleConfig> globalRules) {
        String ruleId = record.get("rule_id", String.class);
        boolean isGlobalReference = record.get("is_global_reference", Boolean.class);

        if (isGlobalReference && globalRules.containsKey(ruleId)) {
            // 引用全局规则，可能有局部覆盖
            RuleConfig globalRule = globalRules.get(ruleId);
            return RuleConfig.builder()
                .id(ruleId)
                .name(getStringValueOrDefault(record, "rule_name", globalRule.name()))
                .priority(getIntegerValueOrDefault(record, "priority", globalRule.priority()))
                .condition(getStringValueOrDefault(record, "condition_expr", globalRule.condition()))
                .handle(getHandlerValueOrDefault(record, globalRule.handle()))
                .build();
        } else {
            // 私有规则
            return convertToRuleConfig(record);
        }
    }

    /**
     * 将数据库记录转换为ProcessNodeConfig（从流程链-节点关联表）
     */
    private ProcessNodeConfig convertToProcessNodeConfigFromChain(Record record, Map<String, ProcessNodeConfig> globalNodes) {
        String nodeId = record.get("node_id", String.class);
        boolean isGlobalReference = record.get("is_global_reference", Boolean.class);

        if (isGlobalReference && globalNodes.containsKey(nodeId)) {
            // 引用全局节点，可能有局部覆盖
            ProcessNodeConfig globalNode = globalNodes.get(nodeId);
            return ProcessNodeConfig.builder()
                .id(nodeId)
                .name(getStringValueOrDefault(record, "node_name", globalNode.name()))
                .next(getStringValueOrDefault(record, "next_node_id", globalNode.next()))
                .type(getNodeTypeValueOrDefault(record, "node_type", globalNode.type()))
                .ruleChain(getStringValueOrDefault(record, "rule_chain_id", globalNode.ruleChain()))
                .properties(getPropertiesValueOrDefault(record, "properties", globalNode.properties()))
                .build();
        } else {
            // 私有节点
            return convertToProcessNodeConfig(record);
        }
    }

    /**
     * 将数据库记录转换为RuleConfig（从全局规则表或规则链关联表）
     */
    public RuleConfig convertToRuleConfig(Record record) {
        RuleConfig.Handler.Type handlerType = RuleConfig.Handler.Type.valueOf(
            record.get("handler_type", String.class));
        String handlerClass = record.get("handler_class", String.class);
        Map<String, Object> handlerParams = parseJsonToMap(record.get("handler_parameters", JSONB.class));

        return RuleConfig.builder()
            .id(record.get("id", String.class))
            .name(record.get("name", String.class))
            .priority(record.get("priority", Integer.class))
            .condition(record.get("condition_expr", String.class))
            .handle(new RuleConfig.Handler(handlerType, handlerClass, handlerParams))
            .build();
    }

    public ProcessNodeConfig convertToProcessNodeConfig(Record record) {
        ProcessNodeConfig.NodeType nodeType = ProcessNodeConfig.NodeType.valueOf(
            record.get("node_type", String.class));
        Map<String, Object> properties = parseJsonToMap(record.get("properties", JSONB.class));

        return ProcessNodeConfig.builder()
            .id(record.get("id", String.class))
            .name(record.get("name", String.class))
            .type(nodeType)
            .ruleChain(record.get("rule_chain_id", String.class))
            .properties(properties)
            .build();
    }

    // =============== 配置转换方法 ===============

    public RuleChainConfig convertToRuleChainConfig(RuleChainDto dto, Map<String, RuleConfig> globalRules) {
        // 这里需要查询该规则链的具体规则...
        // 实际实现中会在调用处提供完整的rules列表
        return RuleChainConfig.builder()
            .id(dto.id())
            .name(dto.name())
            .version(dto.version())
            .description(dto.description())
            .rules(Collections.emptyList()) // 会在外部设置
            .build();
    }

    // =============== 保存方法 ===============

    /**
     * 保存规则链配置
     */
    public void saveRuleChainConfig(DSLContext dsl, RuleChainConfig config) {
        // 1. 保存或更新规则链基本信息
        dsl.insertInto(table(RULE_CHAINS))
            .set(field("id"), config.id())
            .set(field("name"), config.name())
            .set(field("version"), config.version())
            .set(field("description"), config.description())
            .onConflict(field("id"))
            .doUpdate()
            .set(field("name"), config.name())
            .set(field("version"), config.version())
            .set(field("description"), config.description())
            .execute();

        // 2. 删除旧的规则关联
        dsl.deleteFrom(table(RULE_CHAIN_RULES))
            .where(field("rule_chain_id").eq(config.id()))
            .execute();

        // 3. 保存新的规则关联
        if (config.rules() != null && !config.rules().isEmpty()) {
            for (int i = 0; i < config.rules().size(); i++) {
                RuleConfig rule = config.rules().get(i);
                saveRuleChainRule(dsl, config.id(), rule, i + 1);
            }
        }
    }

    /**
     * 保存流程链配置
     */
    public void saveProcessChainConfig(DSLContext dsl, ProcessChainConfig config) {
        // 1. 保存或更新流程链基本信息
        dsl.insertInto(table(PROCESS_CHAINS))
            .set(field("id"), config.id())
            .set(field("name"), config.name())
            .set(field("version"), config.version())
            .set(field("description"), config.description())
            .onConflict(field("id"))
            .doUpdate()
            .set(field("name"), config.name())
            .set(field("version"), config.version())
            .set(field("description"), config.description())
            .execute();

        // 2. 删除旧的节点关联和网关条件
        dsl.deleteFrom(table(GATEWAY_CONDITIONS))
            .where(field("process_chain_id").eq(config.id()))
            .execute();
        dsl.deleteFrom(table(PROCESS_CHAIN_NODES))
            .where(field("process_chain_id").eq(config.id()))
            .execute();

        // 3. 保存新的节点关联
        if (config.nodes() != null && !config.nodes().isEmpty()) {
            for (int i = 0; i < config.nodes().size(); i++) {
                ProcessNodeConfig node = config.nodes().get(i);
                saveProcessChainNode(dsl, config.id(), node, i + 1);

                // 保存网关条件（如果是Gateway节点）
                if (node.type() == ProcessNodeConfig.NodeType.GATEWAY &&
                    node.conditions() != null && !node.conditions().isEmpty()) {
                    saveGatewayConditions(dsl, config.id(), node.id(), node.conditions());
                }
            }
        }
    }

    /**
     * 保存全局规则
     */
    public void saveGlobalRule(DSLContext dsl, GlobalRuleConfig config) {
        dsl.insertInto(table(GLOBAL_RULES))
            .set(field("id"), config.id())
            .set(field("name"), config.name())
            .set(field("priority"), config.priority())
            .set(field("condition_expr"), config.condition())
            .set(field("handler_type"), config.handle().type().name())
            .set(field("handler_class"), config.handle().handler())
            .set(field("handler_parameters"), JSONB.valueOf(mapToJson(config.handle().parameters())))
            .onConflict(field("id"))
            .doUpdate()
            .set(field("name"), config.name())
            .set(field("priority"), config.priority())
            .set(field("condition_expr"), config.condition())
            .set(field("handler_type"), config.handle().type().name())
            .set(field("handler_class"), config.handle().handler())
            .set(field("handler_parameters"), JSONB.valueOf(mapToJson(config.handle().parameters())))
            .execute();
    }

    /**
     * 保存全局流程节点
     */
    public void saveGlobalProcessNode(DSLContext dsl, ProcessNodeConfig config) {
        dsl.insertInto(table(GLOBAL_NODES))
            .set(field("id"), config.id())
            .set(field("name"), config.name())
            .set(field("next_node_id"), config.next())
            .set(field("node_type"), config.type().name())
            .set(field("rule_chain_id"), config.ruleChain())
            .set(field("properties"), JSONB.valueOf(mapToJson(config.properties())))
            .onConflict(field("id"))
            .doUpdate()
            .set(field("name"), config.name())
            .set(field("next_node_id"), config.next())
            .set(field("node_type"), config.type().name())
            .set(field("rule_chain_id"), config.ruleChain())
            .set(field("properties"), JSONB.valueOf(mapToJson(config.properties())))
            .execute();
    }

    // =============== 私有辅助方法 ===============

    private void saveRuleChainRule(DSLContext dsl, String ruleChainId, RuleConfig rule, int order) {
        // 这里简化处理，实际中需要判断是否为全局引用
        boolean isGlobalReference = false; // 根据实际逻辑判断

        dsl.insertInto(table(RULE_CHAIN_RULES))
            .set(field("rule_chain_id"), ruleChainId)
            .set(field("rule_id"), rule.id())
            .set(field("rule_name"), rule.name())
            .set(field("priority"), rule.priority())
            .set(field("condition_expr"), rule.condition())
            .set(field("handler_type"), rule.handle().type().name())
            .set(field("handler_class"), rule.handle().handler())
            .set(field("handler_parameters"), JSONB.valueOf(mapToJson(rule.handle().parameters())))
            .set(field("is_global_reference"), isGlobalReference)
            .set(field("rule_order"), order)
            .execute();
    }

    private void saveProcessChainNode(DSLContext dsl, String processChainId, ProcessNodeConfig node, int order) {
        // 这里简化处理，实际中需要判断是否为全局引用
        boolean isGlobalReference = false; // 根据实际逻辑判断

        dsl.insertInto(table(PROCESS_CHAIN_NODES))
            .set(field("process_chain_id"), processChainId)
            .set(field("node_id"), node.id())
            .set(field("node_name"), node.name())
            .set(field("next_node_id"), node.next())
            .set(field("node_type"), node.type().name())
            .set(field("rule_chain_id"), node.ruleChain())
            .set(field("properties"), JSONB.valueOf(mapToJson(node.properties())))
            .set(field("is_global_reference"), isGlobalReference)
            .set(field("node_order"), order)
            .execute();
    }

    private void saveGatewayConditions(DSLContext dsl, String processChainId, String nodeId, List<GatewayConditionConfig> conditions) {
        for (int i = 0; i < conditions.size(); i++) {
            GatewayConditionConfig condition = conditions.get(i);
            dsl.insertInto(table(GATEWAY_CONDITIONS))
                .set(field("process_chain_id"), processChainId)
                .set(field("node_id"), nodeId)
                .set(field("condition_expr"), condition.condition())
                .set(field("next_node_id"), condition.next())
                .set(field("condition_order"), i + 1)
                .execute();
        }
    }

    // =============== 辅助工具方法 ===============

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonToMap(JSONB jsonb) {
        if (jsonb == null || jsonb.data() == null) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(jsonb.data(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("解析JSONB失败: {}", jsonb.data(), e);
            return new HashMap<>();
        }
    }

    private String mapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.warn("转换Map为JSON失败", e);
            return "{}";
        }
    }

    private String getStringValueOrDefault(Record record, String fieldName, String defaultValue) {
        String value = record.get(fieldName, String.class);
        return value != null ? value : defaultValue;
    }

    private Integer getIntegerValueOrDefault(Record record, String fieldName, Integer defaultValue) {
        Integer value = record.get(fieldName, Integer.class);
        return value != null ? value : defaultValue;
    }

    private ProcessNodeConfig.NodeType getNodeTypeValueOrDefault(Record record, String fieldName, ProcessNodeConfig.NodeType defaultValue) {
        String value = record.get(fieldName, String.class);
        return value != null ? ProcessNodeConfig.NodeType.valueOf(value) : defaultValue;
    }

    private Map<String, Object> getPropertiesValueOrDefault(Record record, String fieldName, Map<String, Object> defaultValue) {
        JSONB jsonb = record.get(fieldName, JSONB.class);
        if (jsonb != null) {
            return parseJsonToMap(jsonb);
        }
        return defaultValue != null ? defaultValue : new HashMap<>();
    }

    private RuleConfig.Handler getHandlerValueOrDefault(Record record, RuleConfig.Handler defaultValue) {
        String handlerType = record.get("handler_type", String.class);
        String handlerClass = record.get("handler_class", String.class);
        JSONB handlerParams = record.get("handler_parameters", JSONB.class);

        if (handlerType != null && handlerClass != null) {
            return new RuleConfig.Handler(
                RuleConfig.Handler.Type.valueOf(handlerType),
                handlerClass,
                parseJsonToMap(handlerParams)
            );
        }
        return defaultValue;
    }
}
