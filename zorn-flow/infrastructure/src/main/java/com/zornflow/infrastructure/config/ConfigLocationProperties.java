package com.zornflow.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/28 22:46
 **/

@Data
@Component
@ConfigurationProperties(prefix = "zornflow.config")
public class ConfigLocationProperties {

  /** 根目录，默认保持向后兼容 */
  private String root = "classpath:/zornflow/";

  /** 子目录名，均可被外部覆盖 */
  private String rules     = "rules";
  private String nodes     = "nodes";
  private String ruleChains = "rule-chains";
  private String flows     = "flow-chains";
}
