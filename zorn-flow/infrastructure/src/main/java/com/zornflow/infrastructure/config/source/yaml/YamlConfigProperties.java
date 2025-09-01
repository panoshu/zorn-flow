package com.zornflow.infrastructure.config.source.yaml;

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
public class YamlConfigProperties {
  private String ruleChainsPath = "classpath*:zornflow/rule-chains/*.yml";
  private String sharedRulesPath = "classpath*:zornflow/shared-rules/*.yml";
  private String processChainsPath = "classpath*:zornflow/process-chains/*.yml";
  private String sharedNodesPath = "classpath*:zornflow/shared-nodes/*.yml";

}
