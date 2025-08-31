package com.zornflow.infrastructure.config1.source.yaml;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/31 4:07
 */
@Data
@Component
@ConfigurationProperties(prefix = "zornflow.config.yaml")
public class YamlRuleChainProperties {

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
   * 全局规则配置路径
   */
  private String sharedRulesPath = "classpath*:zornflow/rules/*.yml";

}
