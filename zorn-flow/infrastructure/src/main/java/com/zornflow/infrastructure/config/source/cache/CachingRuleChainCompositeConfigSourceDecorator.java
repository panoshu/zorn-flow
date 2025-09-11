package com.zornflow.infrastructure.config.source.cache;

import com.zornflow.domain.common.config.source.ReadWriteConfigSource;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * RuleChainCompositeConfigSource 的缓存装饰器。
 * 继承自通用的缓存装饰器基类，只提供特定于规则链的配置。
 * 使用 @Primary 注解，使其成为 Spring 容器中 ReadWriteConfigSource<RuleChainConfig> 类型的首选 Bean
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/1 16:33
 **/

@Service
@Primary
public class CachingRuleChainCompositeConfigSourceDecorator extends AbstractCachingCompositeConfigSourceDecorator<RuleChainConfig> {

  private static final String CACHE_NAME = "ruleChains";

  /**
   * 构造函数。
   *
   * @param delegate     被装饰的原始组合数据源。使用@Qualifier来精确指定注入非Primary的Bean。
   * @param cacheManager Spring的缓存管理器。
   */
  public CachingRuleChainCompositeConfigSourceDecorator(
    @Qualifier("ruleChainCompositeConfigSource") ReadWriteConfigSource<RuleChainConfig> delegate,
    CacheManager cacheManager
  ) {
    super(delegate, cacheManager, CACHE_NAME);
  }
}
