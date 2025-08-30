package com.zornflow.infrastructure.config.dataaccess.yaml;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * YAML配置源属性配置
 * 使用ConfigurationProperties统一管理YAML配置源的相关配置
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/30
 */
@Data
@Component
@ConfigurationProperties(prefix = "zornflow.config.yaml")
public class YamlConfigProperties {

  /**
   * 是否启用YAML配置源
   */
  private boolean enabled = true;

  /**
   * 基础配置路径
   */
  private String basePath = "classpath*:zornflow/**/*.yml";

  /**
   * 规则链配置路径
   */
  private String ruleChainsPath = "classpath*:zornflow/rule-chains/*.yml";

  /**
   * 流程链配置路径
   */
  private String processChainsPath = "classpath*:zornflow/flow-chains/*.yml";

  /**
   * 全局规则配置路径
   */
  private String globalRulesPath = "classpath*:zornflow/rules/*.yml";

  /**
   * 全局节点配置路径
   */
  private String globalNodesPath = "classpath*:zornflow/nodes/*.yml";
}
