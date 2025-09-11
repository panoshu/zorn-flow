package com.zornflow.infrastructure.config.source.cache;

import com.zornflow.domain.common.config.model.ModelConfig;
import com.zornflow.domain.common.config.source.ReadWriteConfigSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * 组合数据源缓存装饰器的抽象基类。
 * 实现了通用的缓存读写逻辑，使用了 Spring Cache Manager。
 * 采用高性能的原子性 "get-or-loan" 缓存策略，避免了手动锁。
 *
 * @param <T> 配置模型的类型，必须继承自 ModelConfig
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/1 16:07
 **/
@Slf4j
public abstract class AbstractCachingCompositeConfigSourceDecorator<T extends ModelConfig>
  implements ReadWriteConfigSource<T> {

  private final String allCacheKey;
  private final ReadWriteConfigSource<T> delegate;
  private final Cache cache;
  private final String itemKeyPrefix; // 新增：用于单项缓存键的前缀

  protected AbstractCachingCompositeConfigSourceDecorator(
    ReadWriteConfigSource<T> delegate,
    CacheManager cacheManager,
    String cacheName
  ) {
    log.info("正在为 '{}' 初始化缓存装饰器，使用缓存 '{}'...", delegate.getSourceName(), cacheName);
    this.delegate = delegate;
    this.cache = cacheManager.getCache(cacheName);
    this.allCacheKey = "__ALL_" + cacheName.toUpperCase() + "__";
    this.itemKeyPrefix = cacheName + "::"; // 新增：初始化前缀，例如 "rules::"

    if (this.cache == null) {
      throw new IllegalStateException("缓存 '" + cacheName + "' 未配置。请在您的缓存配置中定义它。");
    }
  }

  /**
   * 主动热刷新缓存：从委托源重新读取所有配置，并用新数据完全替换缓存内容。
   */
  public void refresh() {
    log.info("接收到刷新请求，正在从委托源为缓存 '{}' 重新加载所有数据...", cache.getName());
    try {
      // 1. 从底层数据源获取最新的全量数据
      Map<String, T> allConfigs = delegate.loadAll();

      // 2. 清空所有旧的缓存条目
      cache.clear();

      // 3. 将新的数据填充到缓存中
      if (allConfigs != null) {
        allConfigs.forEach((id, config) -> cache.put(generateItemCacheKey(id), config)); // 使用带前缀的键
        cache.put(allCacheKey, allConfigs); // 填充loadAll缓存
      }
      log.info("缓存 '{}' 热刷新成功，共加载 {} 条配置。", cache.getName(), allConfigs != null ? allConfigs.size() : 0);
    } catch (Exception e) {
      log.error("热刷新缓存 '{}' 失败。", cache.getName(), e);
    }
  }


  @Override
  public Optional<T> load(String id) {
    String cacheKey = generateItemCacheKey(id); // 使用带前缀的键
    log.debug("正在尝试从缓存 '{}' 中加载键 '{}'...", cache.getName(), cacheKey);
    try {
      T config = cache.get(cacheKey, () -> {
        try {
          log.debug("键 '{}' 在缓存 '{}' 中未命中，正在从委托源加载 ID '{}'...", cacheKey, cache.getName(), id);
          return delegate.load(id).orElse(null);
        } catch (IOException e) {
          throw new CacheLoadException("加载配置失败，ID: " + id, e);
        }
      });
      return Optional.ofNullable(config);
    } catch (CacheLoadException e) {
      log.error("从缓存加载 ID '{}' 时发生错误。", id, e.getCause());
      return Optional.empty();
    }
  }

  @Override
  public Map<String, T> loadAll() {
    log.debug("正在尝试从缓存 '{}' 中加载所有配置...", cache.getName());
    try {
      return cache.get(allCacheKey, () -> {
        try {
          log.debug("loadAll 在缓存 '{}' 中未命中，正在从委托源加载...", cache.getName());
          Map<String, T> allConfigs = delegate.loadAll();
          if (allConfigs != null) {
            // 关键：在填充单项缓存时，也使用带前缀的键
            allConfigs.forEach((id, config) -> cache.putIfAbsent(generateItemCacheKey(id), config));
          }
          return allConfigs;
        } catch (IOException e) {
          throw new CacheLoadException("加载全部配置失败", e);
        }
      });
    } catch (CacheLoadException e) {
      log.error("从缓存加载全部配置时发生错误。", e.getCause());
      return Map.of();
    }
  }

  @Override
  public Optional<T> save(T modelConfig) throws IOException {
    String id = modelConfig.id();
    String cacheKey = generateItemCacheKey(id); // 使用带前缀的键
    log.debug("正在保存 ID '{}'，将更新缓存键 '{}'。", id, cacheKey);

    // 1. 调用委托对象执行实际的保存操作，并获取返回的Optional
    Optional<T> savedModelOptional = delegate.save(modelConfig);

    // 2. 如果保存成功并返回了更新后的对象，则精确更新缓存
    savedModelOptional.ifPresent(savedModel -> {
      cache.put(cacheKey, savedModel); // 使用返回的最新对象更新单项缓存
      cache.evict(allCacheKey);     // 使全量缓存失效
      log.info("键 '{}' 已在缓存 '{}' 中更新，全量缓存已失效。", cacheKey, cache.getName());
    });

    return savedModelOptional;
  }

  @Override
  public void delete(String id) {
    String cacheKey = generateItemCacheKey(id); // 使用带前缀的键
    log.debug("正在删除 ID '{}'，将从缓存中移除键 '{}'。", id, cacheKey);

    // 1. 先执行实际的删除操作
    delegate.delete(id);

    // 2. 精细化更新缓存
    cache.evict(cacheKey); // 移除单个条目
    cache.evict(allCacheKey); // 使全量缓存失效
    log.info("键 '{}' 已从缓存 '{}' 中移除，全量缓存已失效。", cacheKey, cache.getName());
  }

  @Override
  public String getSourceName() {
    return delegate.getSourceName() + " (带缓存)";
  }

  @Override
  public ConfigSourceType getSourceType() {
    return ConfigSourceType.CACHED_COMPOSITE;
  }

  @Override
  public boolean available() {
    return delegate.available();
  }

  /**
   * 生成带前缀的单项缓存键。
   *
   * @param id 配置的原始ID
   * @return 唯一的缓存键，例如 "rules::some-rule-id"
   */
  private String generateItemCacheKey(String id) {
    return this.itemKeyPrefix + id;
  }

  public static class CacheLoadException extends RuntimeException {
    public CacheLoadException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
