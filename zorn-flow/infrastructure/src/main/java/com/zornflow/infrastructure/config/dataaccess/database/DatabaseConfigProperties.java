package com.zornflow.infrastructure.config.dataaccess.database;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 数据库配置源属性配置
 * 使用ConfigurationProperties统一管理数据库配置源的相关配置
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/30
 */
@Data
@Component
@ConfigurationProperties(prefix = "zornflow.config.database")
public class DatabaseConfigProperties {

  /**
   * 是否启用数据库配置源
   */
  private boolean enabled = true;

  /**
   * 数据库表名配置
   */
  private TableNames tableNames = new TableNames();

  @Data
  public static class TableNames {
    /**
     * 全局规则表名
     */
    private String globalRules = "zornflow_global_rules";

    /**
     * 全局流程节点表名
     */
    private String globalNodes = "zornflow_global_nodes";

    /**
     * 规则链配置表名
     */
    private String ruleChains = "zornflow_rule_chains";

    /**
     * 规则链-规则关联表名
     */
    private String ruleChainRules = "zornflow_rule_chain_rules";

    /**
     * 流程链配置表名
     */
    private String processChains = "zornflow_process_chains";

    /**
     * 流程链-节点关联表名
     */
    private String processChainNodes = "zornflow_process_chain_nodes";

    /**
     * 网关条件配置表名
     */
    private String gatewayConditions = "zornflow_gateway_conditions";
  }
}
