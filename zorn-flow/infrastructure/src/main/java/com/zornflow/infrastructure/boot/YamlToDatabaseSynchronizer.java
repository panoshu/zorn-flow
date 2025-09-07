package com.zornflow.infrastructure.boot;

import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.source.database.DatabaseProcessChainConfigSource;
import com.zornflow.infrastructure.config.source.database.DatabaseRuleChainConfigSource;
import com.zornflow.infrastructure.config.source.yaml.YamlProcessChainConfigSource;
import com.zornflow.infrastructure.config.source.yaml.YamlRuleChainConfigSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 在应用启动时，将 YAML 配置文件同步到数据库。
 * 只有在 application.yml 中配置 zornflow.sync.yaml-to-db.enabled=true 时才会执行。
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "zornflow.sync.yaml-to-db.enabled", havingValue = "true")
public class YamlToDatabaseSynchronizer implements CommandLineRunner {

  private final YamlRuleChainConfigSource yamlRuleChainSource;
  private final DatabaseRuleChainConfigSource databaseRuleChainSource;
  private final YamlProcessChainConfigSource yamlProcessChainSource;
  private final DatabaseProcessChainConfigSource databaseProcessChainSource;

  @Override
  public void run(String... args) {
    log.info("===== 开始执行 YAML 到数据库的配置同步 =====");

    syncRuleChains();
    syncProcessChains();

    log.info("===== YAML 到数据库的配置同步完成 =====");
  }

  private void syncRuleChains() {
    try {
      log.info("正在同步规则链（RuleChains）...");
      var ruleChains = yamlRuleChainSource.loadAll();
      if (ruleChains.isEmpty()) {
        log.info("未在 YAML 文件中找到任何规则链配置，跳过同步。");
        return;
      }
      for (RuleChainConfig config : ruleChains.values()) {
        log.debug("正在保存规则链 ID: {}", config.id());
        databaseRuleChainSource.save(config);
      }
      log.info("成功同步 {} 条规则链配置。", ruleChains.size());
    } catch (IOException e) {
      log.error("同步规则链配置失败！", e);
    }
  }

  private void syncProcessChains() {
    try {
      log.info("正在同步流程链（ProcessChains）...");
      var processChains = yamlProcessChainSource.loadAll();
      if (processChains.isEmpty()) {
        log.info("未在 YAML 文件中找到任何流程链配置，跳过同步。");
        return;
      }
      for (ProcessChainConfig config : processChains.values()) {
        log.debug("正在保存流程链 ID: {}", config.id());
        databaseProcessChainSource.save(config);
      }
      log.info("成功同步 {} 条流程链配置。", processChains.size());
    } catch (IOException e) {
      log.error("同步流程链配置失败！", e);
    }
  }
}
