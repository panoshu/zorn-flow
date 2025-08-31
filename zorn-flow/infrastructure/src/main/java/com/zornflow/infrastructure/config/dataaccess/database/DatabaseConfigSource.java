package com.zornflow.infrastructure.config.dataaccess.database;

import com.zornflow.infrastructure.config.model.*;
import com.zornflow.infrastructure.config.source.ReadWriteConfigSource;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;

/**
 * 数据库配置源实现
 * 支持从PostgreSQL数据库读写配置数据
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
@Slf4j
public class DatabaseConfigSource implements ReadWriteConfigSource {

  private final DSLContext dsl;
  private final DatabaseConfigSourceHelper helper = new DatabaseConfigSourceHelper();

  private final DatabaseConfigProperties databaseConfigProperties;

  // 表名常量（使用配置化表名）
  private String GLOBAL_RULES;
  private String GLOBAL_NODES;
  private String RULE_CHAINS;
  private String RULE_CHAIN_RULES;
  private String PROCESS_CHAINS;
  private String PROCESS_CHAIN_NODES;
  private String GATEWAY_CONDITIONS;

  public DatabaseConfigSource(DSLContext dsl, DatabaseConfigProperties databaseConfigProperties) {
    this.dsl = dsl;
    this.databaseConfigProperties = databaseConfigProperties;
    initializeTableNames();
  }

  /**
   * 初始化表名
   */
  private void initializeTableNames() {
    if (databaseConfigProperties != null && databaseConfigProperties.getTableNames() != null) {
      GLOBAL_RULES = databaseConfigProperties.getTableNames().getGlobalRules();
      GLOBAL_NODES = databaseConfigProperties.getTableNames().getGlobalNodes();
      RULE_CHAINS = databaseConfigProperties.getTableNames().getRuleChains();
      RULE_CHAIN_RULES = databaseConfigProperties.getTableNames().getRuleChainRules();
      PROCESS_CHAINS = databaseConfigProperties.getTableNames().getProcessChains();
      PROCESS_CHAIN_NODES = databaseConfigProperties.getTableNames().getProcessChainNodes();
      GATEWAY_CONDITIONS = databaseConfigProperties.getTableNames().getGatewayConditions();
    } else {
      // 默认表名
      GLOBAL_RULES = "zornflow_global_rules";
      GLOBAL_NODES = "zornflow_global_nodes";
      RULE_CHAINS = "zornflow_rule_chains";
      RULE_CHAIN_RULES = "zornflow_rule_chain_rules";
      PROCESS_CHAINS = "zornflow_process_chains";
      PROCESS_CHAIN_NODES = "zornflow_process_chain_nodes";
      GATEWAY_CONDITIONS = "zornflow_gateway_conditions";
    }
  }

  @Override
  public String getSourceName() {
    return "DatabaseConfigSource";
  }

  @Override
  public ConfigSourceType getSourceType() {
    return ConfigSourceType.DATABASE;
  }

  @Override
  public int getPriority() {
    return 50; // 数据库配置源优先级中等
  }

  @Override
  public boolean isAvailable() {
    try {
      dsl.selectOne().fetch();
      return true;
    } catch (Exception e) {
      log.warn("数据库配置源不可用: {}", e.getMessage());
      return false;
    }
  }

  // =============== 读取操作 ===============

  @Override
  public Map<String, RuleChainConfig> loadRuleChainConfigs() {
    try {
      Map<String, RuleChainConfig> result = new HashMap<>();

      // 1. 加载所有规则链基本信息
      Map<String, RuleChainConfig.RuleChainConfigBuilder> builders = dsl.select()
        .from(table(RULE_CHAINS))
        .fetch()
        .stream()
        .collect(Collectors.toMap(
          record -> record.get("id", String.class),
          record -> RuleChainConfig.builder()
            .id(record.get("id", String.class))
            .name(record.get("name", String.class))
            .version(record.get("version", String.class))
            .description(record.get("description", String.class))
        ));

      // 2. 加载全局规则用于引用合并
      Map<String, RuleConfig> globalRules = loadGlobalRules();

      // 3. 为每个规则链加载其规则列表
      for (Map.Entry<String, RuleChainConfig.RuleChainConfigBuilder> entry : builders.entrySet()) {
        String ruleChainId = entry.getKey();
        List<RuleConfig> rules = helper.loadRuleChainRules(dsl, ruleChainId, globalRules, RULE_CHAIN_RULES);

        RuleChainConfig config = entry.getValue()
          .ruleConfigs(rules)
          .build();
        result.put(ruleChainId, config);
      }

      return result;
    } catch (Exception e) {
      log.error("从数据库加载规则链配置失败", e);
      return Collections.emptyMap();
    }
  }

  @Override
  public Optional<RuleChainConfig> loadRuleChainConfig(String ruleChainId) {
    try {
      // 1. 加载规则链基本信息
      Record ruleChainRecord = dsl.select()
        .from(table(RULE_CHAINS))
        .where(field("id").eq(ruleChainId))
        .fetchOne();

      if (ruleChainRecord == null) {
        return Optional.empty();
      }

      // 2. 加载全局规则用于引用合并
      Map<String, RuleConfig> globalRules = loadGlobalRules();

      // 3. 加载规则链的规则列表
      List<RuleConfig> rules = helper.loadRuleChainRules(dsl, ruleChainId, globalRules, RULE_CHAIN_RULES);

      RuleChainConfig config = RuleChainConfig.builder()
        .id(ruleChainRecord.get("id", String.class))
        .name(ruleChainRecord.get("name", String.class))
        .version(ruleChainRecord.get("version", String.class))
        .description(ruleChainRecord.get("description", String.class))
        .ruleConfigs(rules)
        .build();

      return Optional.of(config);
    } catch (Exception e) {
      log.error("从数据库加载规则链配置失败: {}", ruleChainId, e);
      return Optional.empty();
    }
  }

  @Override
  public Map<String, ProcessChainConfig> loadProcessChainConfigs() {
    try {
      Map<String, ProcessChainConfig> result = new HashMap<>();

      // 1. 加载所有流程链基本信息
      Map<String, ProcessChainConfig.ProcessChainConfigBuilder> builders = dsl.select()
        .from(table(PROCESS_CHAINS))
        .fetch()
        .stream()
        .collect(Collectors.toMap(
          record -> record.get("id", String.class),
          record -> ProcessChainConfig.builder()
            .id(record.get("id", String.class))
            .name(record.get("name", String.class))
            .version(record.get("version", String.class))
            .description(record.get("description", String.class))
        ));

      // 2. 加载全局节点用于引用合并
      Map<String, ProcessNodeConfig> globalNodes = loadGlobalNodes();

      // 3. 为每个流程链加载其节点列表
      for (Map.Entry<String, ProcessChainConfig.ProcessChainConfigBuilder> entry : builders.entrySet()) {
        String processChainId = entry.getKey();
        List<ProcessNodeConfig> nodes = helper.loadProcessChainNodes(dsl, processChainId, globalNodes, PROCESS_CHAIN_NODES, GATEWAY_CONDITIONS);

        ProcessChainConfig config = entry.getValue()
          .nodes(nodes)
          .build();
        result.put(processChainId, config);
      }

      return result;
    } catch (Exception e) {
      log.error("从数据库加载流程链配置失败", e);
      return Collections.emptyMap();
    }
  }

  @Override
  public Optional<ProcessChainConfig> loadProcessChainConfig(String processChainId) {
    try {
      // 1. 加载流程链基本信息
      Record processChainRecord = dsl.select()
        .from(table(PROCESS_CHAINS))
        .where(field("id").eq(processChainId))
        .fetchOne();

      if (processChainRecord == null) {
        return Optional.empty();
      }

      // 2. 加载全局节点用于引用合并
      Map<String, ProcessNodeConfig> globalNodes = loadGlobalNodes();

      // 3. 加载流程链的节点列表
      List<ProcessNodeConfig> nodes = helper.loadProcessChainNodes(dsl, processChainId, globalNodes, PROCESS_CHAIN_NODES, GATEWAY_CONDITIONS);

      ProcessChainConfig config = ProcessChainConfig.builder()
        .id(processChainRecord.get("id", String.class))
        .name(processChainRecord.get("name", String.class))
        .version(processChainRecord.get("version", String.class))
        .description(processChainRecord.get("description", String.class))
        .nodes(nodes)
        .build();

      return Optional.of(config);
    } catch (Exception e) {
      log.error("从数据库加载流程链配置失败: {}", processChainId, e);
      return Optional.empty();
    }
  }

  @Override
  public Map<String, RuleConfig> loadGlobalRules() {
    try {
      return dsl.select()
        .from(table(GLOBAL_RULES))
        .fetch()
        .stream()
        .collect(Collectors.toMap(
          record -> record.get("id", String.class),
          helper::convertToRuleConfig
        ));
    } catch (Exception e) {
      log.error("从数据库加载全局规则失败", e);
      return Collections.emptyMap();
    }
  }

  @Override
  public Map<String, ProcessNodeConfig> loadGlobalNodes() {
    try {
      return dsl.select()
        .from(table(GLOBAL_NODES))
        .fetch()
        .stream()
        .collect(Collectors.toMap(
          record -> record.get("id", String.class),
          helper::convertToProcessNodeConfig
        ));
    } catch (Exception e) {
      log.error("从数据库加载全局节点失败", e);
      return Collections.emptyMap();
    }
  }

  @Override
  public boolean refresh() {
    // 数据库配置源不需要特殊的刷新逻辑，因为每次查询都是实时的
    // 这里返回true表示刷新成功
    log.debug("数据库配置源无需刷新");
    return true;
  }

  @Override
  public void clearCache() {
    // 数据库配置源没有缓存需要清除
    log.debug("数据库配置源没有缓存, 无需清除");
  }

  // =============== 写入操作 ===============

  @Override
  @Transactional
  public void saveRuleChainConfig(RuleChainConfig config) {
    try {
      helper.saveRuleChainConfig(dsl, config, RULE_CHAINS, RULE_CHAIN_RULES);
      log.info("保存规则链配置成功: {}", config.id());
    } catch (Exception e) {
      log.error("保存规则链配置失败: {}", config.id(), e);
      throw new RuntimeException("保存规则链配置失败", e);
    }
  }

  @Override
  @Transactional
  public void saveProcessChainConfig(ProcessChainConfig config) {
    try {
      helper.saveProcessChainConfig(dsl, config, PROCESS_CHAINS, PROCESS_CHAIN_NODES, GATEWAY_CONDITIONS);
      log.info("保存流程链配置成功: {}", config.id());
    } catch (Exception e) {
      log.error("保存流程链配置失败: {}", config.id(), e);
      throw new RuntimeException("保存流程链配置失败", e);
    }
  }

  @Override
  @Transactional
  public void saveGlobalRule(GlobalRuleConfig config) {
    try {
      helper.saveGlobalRule(dsl, config, GLOBAL_RULES);
      log.info("保存全局规则成功: {}", config.id());
    } catch (Exception e) {
      log.error("保存全局规则失败: {}", config.id(), e);
      throw new RuntimeException("保存全局规则失败", e);
    }
  }

  @Override
  @Transactional
  public void saveGlobalProcessNode(ProcessNodeConfig config) {
    try {
      helper.saveGlobalProcessNode(dsl, config, GLOBAL_NODES);
      log.info("保存全局流程节点成功: {}", config.id());
    } catch (Exception e) {
      log.error("保存全局流程节点失败: {}", config.id(), e);
      throw new RuntimeException("保存全局流程节点失败", e);
    }
  }

  @Override
  @Transactional
  public void deleteRuleChainConfig(String ruleChainId) {
    try {
      int deleted = dsl.deleteFrom(table(RULE_CHAINS))
        .where(field("id").eq(ruleChainId))
        .execute();
      log.info("删除规则链配置: {} (影响行数: {})", ruleChainId, deleted);
    } catch (Exception e) {
      log.error("删除规则链配置失败: {}", ruleChainId, e);
      throw new RuntimeException("删除规则链配置失败", e);
    }
  }

  @Override
  @Transactional
  public void deleteProcessChainConfig(String processChainId) {
    try {
      int deleted = dsl.deleteFrom(table(PROCESS_CHAINS))
        .where(field("id").eq(processChainId))
        .execute();
      log.info("删除流程链配置: {} (影响行数: {})", processChainId, deleted);
    } catch (Exception e) {
      log.error("删除流程链配置失败: {}", processChainId, e);
      throw new RuntimeException("删除流程链配置失败", e);
    }
  }

  @Override
  @Transactional
  public void deleteGlobalRule(String ruleId) {
    try {
      int deleted = dsl.deleteFrom(table(GLOBAL_RULES))
        .where(field("id").eq(ruleId))
        .execute();
      log.info("删除全局规则: {} (影响行数: {})", ruleId, deleted);
    } catch (Exception e) {
      log.error("删除全局规则失败: {}", ruleId, e);
      throw new RuntimeException("删除全局规则失败", e);
    }
  }

  @Override
  @Transactional
  public void deleteGlobalProcessNode(String nodeId) {
    try {
      int deleted = dsl.deleteFrom(table(GLOBAL_NODES))
        .where(field("id").eq(nodeId))
        .execute();
      log.info("删除全局流程节点: {} (影响行数: {})", nodeId, deleted);
    } catch (Exception e) {
      log.error("删除全局流程节点失败: {}", nodeId, e);
      throw new RuntimeException("删除全局流程节点失败", e);
    }
  }

  @Override
  @Transactional
  public void saveRuleChainConfigs(List<RuleChainConfig> configs) {
    for (RuleChainConfig config : configs) {
      saveRuleChainConfig(config);
    }
  }

  @Override
  @Transactional
  public void saveProcessChainConfigs(List<ProcessChainConfig> configs) {
    for (ProcessChainConfig config : configs) {
      saveProcessChainConfig(config);
    }
  }

  @Override
  @Transactional
  public void saveGlobalRules(List<GlobalRuleConfig> configs) {
    for (GlobalRuleConfig config : configs) {
      saveGlobalRule(config);
    }
  }

  @Override
  @Transactional
  public void saveGlobalProcessNodes(List<ProcessNodeConfig> configs) {
    for (ProcessNodeConfig config : configs) {
      saveGlobalProcessNode(config);
    }
  }

  @Override
  @Transactional
  public void clearAll() {
    try {
      // 按照外键依赖关系的逆序删除
      dsl.deleteFrom(table(GATEWAY_CONDITIONS)).execute();
      dsl.deleteFrom(table(PROCESS_CHAIN_NODES)).execute();
      dsl.deleteFrom(table(RULE_CHAIN_RULES)).execute();
      dsl.deleteFrom(table(PROCESS_CHAINS)).execute();
      dsl.deleteFrom(table(RULE_CHAINS)).execute();
      dsl.deleteFrom(table(GLOBAL_NODES)).execute();
      dsl.deleteFrom(table(GLOBAL_RULES)).execute();
      log.info("清空所有配置数据成功");
    } catch (Exception e) {
      log.error("清空所有配置数据失败", e);
      throw new RuntimeException("清空所有配置数据失败", e);
    }
  }
}
