package com.zornflow.infrastructure.config.source.composite;

import com.zornflow.domain.common.config.model.ModelConfig;
import com.zornflow.domain.common.config.source.ConfigSource;
import com.zornflow.domain.common.config.source.ReadWriteConfigSource;
import com.zornflow.domain.common.config.source.ReadableConfigSource;
import com.zornflow.domain.common.config.source.WriteableConfigSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/1 15:25
 **/

@Slf4j
public abstract sealed class AbstractCompositeConfigSource<T extends ModelConfig> implements ReadWriteConfigSource<T>
  permits ProcessChainCompositeConfigSource, RuleChainCompositeConfigSource {

  // 数据源列表，应按优先级排序（例如，通过 Spring 的 @Order）
  private final List<ConfigSource<T>> sources;

  public AbstractCompositeConfigSource(List<ConfigSource<T>> sources) {
    // 创建一个副本以防外部修改
    this.sources = List.copyOf(sources);
    // 在初始化时执行重复ID的检查
    validateForDuplicateIds();
  }

  /**
   * 根据ID加载配置。
   * 它会按优先级顺序遍历所有可读数据源，找到第一个匹配的配置并立即返回。
   */
  @Override
  public Optional<T> load(String id) throws IOException {

    for (ConfigSource<T> source : sources) {
      if (source instanceof ReadableConfigSource<T> readableSource) {
        Optional<T> config = readableSource.load(id);
        if (config.isPresent()) {
          log.debug("ID为 '{}' 的配置在源 '{}' 中找到。", id, source.getSourceName());
          return config;
        }
      }
    }
    log.debug("在所有数据源中都未找到ID为 '{}' 的配置。", id);
    return Optional.empty();
  }

  /**
   * 加载所有配置。
   * 它会合并所有可读数据源的配置，高优先级的数据源会覆盖低优先级的同ID配置。
   */
  @Override
  public Map<String, T> loadAll() throws IOException {
    Map<String, T> combinedResult = new LinkedHashMap<>();
    // 从低优先级到高优先级进行合并，以确保高优先级能够覆盖
    List<ConfigSource<T>> reversedSources = new java.util.ArrayList<>(sources);
    Collections.reverse(reversedSources);

    for (ConfigSource<T> source : reversedSources) {
      if (source instanceof ReadableConfigSource<T> readableSource && readableSource.available()) {
        combinedResult.putAll(readableSource.loadAll());
      }
    }
    return combinedResult;
  }

  /**
   * 保存一个配置。
   * 策略：
   * 1. 如果该ID的配置已存在于某个源中，则在那个源上进行更新（前提是该源可写）。
   * 2. 如果是全新的配置，则保存在优先级最高地可写数据源中。
   */
  @Override
  public Optional<T> save(T modelConfig) throws IOException {
    String id = modelConfig.id();

    // 1. 查找已存在的源
    for (ConfigSource<T> source : sources) {
      if (source instanceof ReadableConfigSource<T> readableSource) {
        if (readableSource.load(id).isPresent()) {
          if (source instanceof WriteableConfigSource<T> writeableSource) {
            log.info("ID为 '{}' 的配置存在于源 '{}' 中，将在此源上执行更新。", id, source.getSourceName());
            return writeableSource.save(modelConfig);
          } else {
            throw new UnsupportedOperationException(
              "配置 (ID: " + id + ") 存在于只读源 '" + source.getSourceName() + "' 中，无法保存。"
            );
          }
        }
      }
    }

    // 2. 如果不存在，则在最高优先级的可写源中创建
    Optional<WriteableConfigSource<T>> highestPriorityWritableSource = sources.stream()
      .filter(WriteableConfigSource.class::isInstance)
      .map(s -> (WriteableConfigSource<T>) s)
      .findFirst();

    if (highestPriorityWritableSource.isPresent()) {
      WriteableConfigSource<T> targetSource = highestPriorityWritableSource.get();
      log.info("ID为 '{}' 的配置是全新的，将在最高优先级的可写源 '{}' 中创建。", id, targetSource.getSourceName());
      targetSource.save(modelConfig);
    } else {
      throw new UnsupportedOperationException("没有配置可写的持久化数据源来保存新配置 (ID: " + id + ")。");
    }
    return Optional.empty();
  }

  /**
   * 删除一个配置。
   * 它会查找包含该ID的第一个数据源，并尝试删除（前提是该源可写）。
   */
  @SneakyThrows
  @Override
  public void delete(String id) {
    for (ConfigSource<T> source : sources) {
      if (source instanceof ReadableConfigSource<T> readableSource) {
        if (readableSource.load(id).isPresent()) {
          if (source instanceof WriteableConfigSource<T> writeableSource) {
            log.info("ID为 '{}' 的配置在源 '{}' 中找到，将执行删除。", id, source.getSourceName());
            writeableSource.delete(id);
            return;
          } else {
            throw new UnsupportedOperationException(
              "配置 (ID: " + id + ") 存在于只读源 '" + source.getSourceName() + "' 中，无法删除。"
            );
          }
        }
      }
    }
    log.warn("尝试删除一个不存在的配置 (ID: {})，操作被忽略。", id);
  }

  /**
   * 只要有任何一个子数据源可用，组合数据源就认为可用。
   */
  @Override
  public boolean available() {
    return sources.stream().anyMatch(ConfigSource::available);
  }

  /**
   * 检查不同数据源之间是否存在重复的ID，并发出警告。
   */
  private void validateForDuplicateIds() {
    Map<String, String> idToSourceName = new HashMap<>();
    for (ConfigSource<T> source : sources) {
      if (source instanceof ReadableConfigSource<T> readableSource && readableSource.available()) {
        try {
          readableSource.loadAll().forEach((id, config) -> {
            if (idToSourceName.containsKey(id)) {
              log.warn("ID冲突警告！ID '{}' 同时存在于源 '{}' 和 '{}'。源 '{}' 的配置将优先使用。",
                id, idToSourceName.get(id), source.getSourceName(), source.getSourceName());
            }
            // 始终记录最后（即最高优先级）看到该ID的源
            idToSourceName.put(id, source.getSourceName());
          });
        } catch (IOException e) {
          log.error("在为源 '{}' 进行ID冲突校验时加载配置失败。", source.getSourceName(), e);
        }
      }
    }
  }

  @Override
  public ConfigSourceType getSourceType() {
    return ConfigSourceType.COMPOSITE;
  }
}
