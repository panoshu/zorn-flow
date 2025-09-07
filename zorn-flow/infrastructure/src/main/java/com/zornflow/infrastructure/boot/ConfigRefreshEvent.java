package com.zornflow.infrastructure.boot;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 配置刷新事件。
 * 当需要热刷新配置时，发布此事件。
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/1 16:52
 **/

@Getter
public class ConfigRefreshEvent extends ApplicationEvent {

  private final ConfigType configType;

  /**
   * @param source     事件源
   * @param configType 要刷新的配置类型
   */
  public ConfigRefreshEvent(Object source, ConfigType configType) {
    super(source);
    this.configType = configType;
  }

  public enum ConfigType {
    RULES,
    PROCESSES,
    ALL
  }
}
