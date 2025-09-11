package com.zornflow.infrastructure.config.source.cache;

import com.zornflow.domain.common.config.source.ReadWriteConfigSource;
import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * ProcessChainCompositeConfigSource 的缓存装饰器。
 * 继承自通用的缓存装饰器基类，只提供特定于流程链的配置。
 * 使用 @Primary 注解，使其成为 Spring 容器中 ReadWriteConfigSource<ProcessChainConfig> 类型的首选 Bean。
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/1 16:32
 **/

@Service
@Primary
public class CachingProcessChainCompositeConfigSourceDecorator extends AbstractCachingCompositeConfigSourceDecorator<ProcessChainConfig> {

  private static final String CACHE_NAME = "processChains";

  /**
   * 构造函数。
   *
   * @param delegate     被装饰的原始组合数据源。使用@Qualifier来精确指定注入非Primary的Bean。
   * @param cacheManager Spring的缓存管理器，用于获取具体的缓存实例。
   */
  public CachingProcessChainCompositeConfigSourceDecorator(
    @Qualifier("processChainCompositeConfigSource") ReadWriteConfigSource<ProcessChainConfig> delegate,
    CacheManager cacheManager
  ) {
    super(delegate, cacheManager, CACHE_NAME);
  }
}
