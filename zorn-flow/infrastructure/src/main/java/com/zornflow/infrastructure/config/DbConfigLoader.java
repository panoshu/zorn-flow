package com.zornflow.infrastructure.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zornflow.infrastructure.config.model.ProcessNode;
import com.zornflow.infrastructure.config.model.Rule;
import com.zornflow.infrastructure.config.model.RuleChain;
import com.zornflow.domain.rule.types.HandlerType;
import com.zornflow.infrastructure.config.model.FlowChain;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 数据库配置加载器，与 ClasspathConfigLoader 互斥。
 * 当 spring.profiles.active=dbconfig 时启用。
 */
@Component
@ConditionalOnProperty(name = "zornflow.config.source", havingValue = "db")
@RequiredArgsConstructor
@Slf4j
public class DbConfigLoader {

  private final JdbcTemplate jdbc;
  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

  /* ---------- 内存仓库（同前） ---------- */
  private final Map<String, Rule>        globalRules      = new HashMap<>();
  private final Map<String, ProcessNode> globalNodes      = new HashMap<>();
  private final Map<String, RuleChain>   globalRuleChains = new HashMap<>();
  private final Map<String, FlowChain>   globalFlows      = new HashMap<>();

  /* ---------- 启动时一次性加载 ---------- */
  @PostConstruct
  public void loadAll() {
    loadRules();
    loadNodes();
    loadRuleChains();
    loadFlows();
    log.info("DB config loaded: rules={}, nodes={}, ruleChains={}, flows={}",
      globalRules.size(), globalNodes.size(), globalRuleChains.size(), globalFlows.size());
  }

  /* ---------- 私有加载方法 ---------- */
  private void loadRules() {
    String sql = "SELECT * FROM cfg_rule";
    jdbc.query(sql, rs -> {
      Rule rule = Rule.builder()
        .id(rs.getString("id"))
        .name(rs.getString("name"))
        .priority(rs.getInt("priority"))
        .condition(rs.getString("condition_"))
        .handle(mapper.readValue(
          rs.getString("parameters"),
          new TypeReference<Map<String, Object>>() {}))
        .type(HandlerType.valueOf(rs.getString("handle_type")))
        .handler(rs.getString("handler"))
        .build();
      globalRules.put(rule.id(), rule);
    });
  }

  private void loadNodes() {
    String sql = "SELECT * FROM cfg_node";
    jdbc.query(sql, rs -> {
      ProcessNode node = ProcessNode.builder()
        .id(rs.getString("id"))
        .name(rs.getString("name"))
        .type(com.zornflow.infrastructure.config.model.ProcessNode.NodeType.valueOf(rs.getString("type_")))
        .ruleChain(rs.getString("rule_chain"))
        .properties(mapper.readValue(
          rs.getString("properties"),
          new TypeReference<>() {
          }))
        .build();
      globalNodes.put(node.id(), node);
    });
  }

  private void loadRuleChains() {
    // 1. 链头
    Map<String, RuleChain> chains = new HashMap<>();
    jdbc.query("SELECT * FROM cfg_rule_chain",
      (ResultSetExtractor<RuleChain>) rs -> chains.put(rs.getString("id"),
        RuleChain.builder()
          .id(rs.getString("id"))
          .name(rs.getString("name"))
          .version(rs.getString("version"))
          .description(rs.getString("description"))
          .rules(new ArrayList<>())
          .build()));

    // 2. 明细
    String itemSql = """
                SELECT c.*, r.*
                FROM cfg_rule_chain_item c
                JOIN cfg_rule r ON r.id = c.rule_id
                ORDER BY c.chain_id, c.priority
                """;
    jdbc.query(itemSql, rs -> {
      Rule rule = Rule.builder()
        .id(rs.getString("rule_id"))
        .name(rs.getString("name"))
        .priority(rs.getInt("priority"))
        .condition(rs.getString("condition_"))
        .handle(mapper.readValue(
          rs.getString("parameters"),
          new TypeReference<Map<String, Object>>() {}))
        .type(HandlerType.valueOf(rs.getString("handle_type")))
        .handler(rs.getString("handler"))
        .build();
      chains.get(rs.getString("chain_id")).rules().add(rule);
    });

    globalRuleChains.putAll(chains);
  }

  private void loadFlows() {
    // 1. 链头
    Map<String, FlowChain> chains = new HashMap<>();
    jdbc.query("SELECT * FROM cfg_flow_chain",
      (ResultSetExtractor<FlowChain>) rs -> chains.put(rs.getString("id"),
        FlowChain.builder()
          .id(rs.getString("id"))
          .name(rs.getString("name"))
          .version(rs.getString("version"))
          .description(rs.getString("description"))
          .nodes(new ArrayList<>())
          .build()));

    // 2. 明细
    String itemSql = """
                SELECT c.*, n.*
                FROM cfg_flow_chain_item c
                JOIN cfg_node n ON n.id = c.node_id
                ORDER BY c.chain_id, c.seq_no
                """;
    jdbc.query(itemSql, rs -> {
      ProcessNode node = ProcessNode.builder()
        .id(rs.getString("node_id"))
        .name(rs.getString("name"))
        .type(ProcessNode.NodeType.valueOf(rs.getString("type_")))
        .ruleChain(rs.getString("c.rule_chain") != null
          ? rs.getString("c.rule_chain")
          : rs.getString("n.rule_chain"))
        .properties(mapper.readValue(
          rs.getString("c.properties") != null
            ? rs.getString("c.properties")
            : rs.getString("n.properties"),
          new TypeReference<>() {
          }))
        .build();
      chains.get(rs.getString("chain_id")).nodes().add(node);
    });

    globalFlows.putAll(chains);
  }

  private Map<String, Object> safeMap(String json) {
    if (json == null || json.isBlank()) return Map.of();
    try {
      return mapper.readValue(json, new TypeReference<>() {});
    } catch (JsonProcessingException e) {
      log.warn("Bad json: {}", json, e);
      return Map.of();
    }
  }

  /* ---------- 只读查询 API（与 ClasspathConfigLoader 保持一致） ---------- */
  public Optional<Rule>        rule(String id)       { return Optional.ofNullable(globalRules.get(id)); }
  public Optional<ProcessNode> node(String id)       { return Optional.ofNullable(globalNodes.get(id)); }
  public Optional<RuleChain>   ruleChain(String id)  { return Optional.ofNullable(globalRuleChains.get(id)); }
  public Optional<FlowChain>   flow(String id)      { return Optional.ofNullable(globalFlows.get(id)); }
}
