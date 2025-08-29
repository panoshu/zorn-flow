package com.zornflow.infrastructure.config.configuration;

import com.zornflow.infrastructure.config.source.ConfigSourceManager;
import com.zornflow.infrastructure.config.source.contract.ReadableConfigSource;
import com.zornflow.infrastructure.config.source.contract.WritableConfigSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * 配置源自动配置类
 * 负责自动发现和注册所有可用的配置源到ConfigSourceManager
 * <p>
 * 设计原则：
 * - 自动发现：利用Spring的依赖注入机制自动发现配置源实现
 * - 优先级管理：根据配置源的优先级自动排序
 * - 可扩展性：新增配置源实现会自动被发现和注册
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
public class ConfigSourceAutoConfiguration {

  /**
   * 创建配置源管理器并自动注册所有可用的配置源
   *
   * @param readableSources 所有可读配置源实现
   * @param writableSources 所有可写配置源实现
   * @return 配置好的配置源管理器
   */
  @Bean
  public ConfigSourceManager configSourceManager(
    ObjectProvider<List<ReadableConfigSource>> readableSources,
    ObjectProvider<List<WritableConfigSource>> writableSources) {

    ConfigSourceManager manager = new ConfigSourceManager();

    // 注册所有可读配置源
    readableSources.ifAvailable(sources -> {
      for (ReadableConfigSource source : sources) {
        if (!(source instanceof WritableConfigSource)) {
          // 只注册纯只读配置源，可写配置源会在下面单独注册
          manager.registerReadableSource(source);
        }
      }
    });

    // 注册所有可写配置源（同时也是可读的）
    writableSources.ifAvailable(sources -> {
      for (WritableConfigSource source : sources) {
        manager.registerWritableSource(source);
      }
    });

    log.info("ConfigSourceManager auto-configuration completed");
    return manager;
  }
}
