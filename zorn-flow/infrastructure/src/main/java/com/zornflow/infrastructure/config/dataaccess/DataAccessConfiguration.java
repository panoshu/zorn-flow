package com.zornflow.infrastructure.config.dataaccess;

import com.zornflow.infrastructure.config.dataaccess.composite.CompositeConfigSource;
import com.zornflow.infrastructure.config.dataaccess.composite.CompositeConfigSourceProperties;
import com.zornflow.infrastructure.config.dataaccess.database.DatabaseConfigProperties;
import com.zornflow.infrastructure.config.dataaccess.yaml.YamlConfigProperties;
import com.zornflow.infrastructure.config.source.ConfigSource;
import com.zornflow.infrastructure.config.source.ReadWriteConfigSource;
import com.zornflow.infrastructure.config.dataaccess.yaml.YamlConfigSource;
import com.zornflow.infrastructure.config.dataaccess.database.DatabaseConfigSource;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.List;

/**
 * 组合配置源配置类
 * 彻底重构版本：移除YAML适配器，直接使用原生YAML配置源
 * 各个配置源各司其职，不强制类型转换
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 3.0
 * @since 2025/8/30
 */
@Slf4j
@Configuration
public class DataAccessConfiguration {

  @Bean
  @ConditionalOnProperty(name = "zornflow.config.yaml.enabled", havingValue = "true", matchIfMissing = true)
  public YamlConfigProperties yamlConfigProperties(){
    return new YamlConfigProperties();
  }

  @Bean
  @ConditionalOnProperty(name = "zornflow.config.yaml.enabled", havingValue = "true", matchIfMissing = true)
  public YamlConfigSource yamlConfigSource(YamlConfigProperties yamlConfigProperties) {
    log.info("创建YAML配置源 - 使用Spring Boot配置管理");
    return new YamlConfigSource(yamlConfigProperties);
  }

  /**
   * 配置事务管理器
   */
  @Bean("zornflowTransactionManager")
  @ConditionalOnProperty(name = "zornflow.config.database.enabled", havingValue = "true", matchIfMissing = true)
  public PlatformTransactionManager transactionManager(ObjectProvider<DataSource> dataSourceObjectProvider) {
    DataSource dataSource = dataSourceObjectProvider.getIfAvailable();
    if (dataSource == null) {
      throw new IllegalStateException("DataSource is required but not configured");
    }
    return new DataSourceTransactionManager(dataSource);
  }

  @Bean
  @ConditionalOnProperty(name = "zornflow.config.database.enabled", havingValue = "true", matchIfMissing = true)
  public DatabaseConfigProperties databaseConfigProperties() {
    return new DatabaseConfigProperties();
  }

  /**
   * 创建数据库配置源Bean
   */
  @Bean("databaseConfigSource")
  @ConditionalOnProperty(name = "zornflow.config.database.enabled", havingValue = "true", matchIfMissing = true)
  public ReadWriteConfigSource databaseConfigSource(
    ObjectProvider<DSLContext> dslContextObjectProvider,
    DatabaseConfigProperties databaseConfigProperties) {
    DSLContext dslContext = dslContextObjectProvider.getIfAvailable();
    if (dslContext == null) {
      throw new IllegalStateException("DSLContext 不存在！请确保主应用模块已添加 spring-boot-starter-jooq 且已正确配置 DataSource");
    }
    log.info("创建数据库配置源");
    return new DatabaseConfigSource(dslContext, databaseConfigProperties);
  }

  @Bean
  @ConditionalOnProperty(name = "zornflow.config.composite.enabled", havingValue = "true", matchIfMissing = true)
  public CompositeConfigSourceProperties compositeConfigSourceProperties() {
    return new CompositeConfigSourceProperties();
  }
  /**
   * 创建组合配置源Bean - 终极版本
   * 使用新的混合构造函数，自动分类配置源类型
   */
  @Bean
  @Primary
  @ConditionalOnProperty(name = "zornflow.config.composite.enabled", havingValue = "true", matchIfMissing = true)
  public CompositeConfigSource compositeConfigSource(
    List<? extends ConfigSource> allConfigSources,  // 所有类型的配置源
    CompositeConfigSourceProperties properties) {

    log.info("创建组合配置源 - 总数: {}", allConfigSources.size());

    // 使用新的混合构造函数，自动分类
    CompositeConfigSource compositeSource = new CompositeConfigSource(allConfigSources);

    // 应用配置
    compositeSource.setCacheEnabled(properties.isCacheEnabled());
    compositeSource.setCacheExpireTime(properties.getCacheExpireTimeMs());

    log.info("组合配置源创建成功 - 缓存: {}, 过期时间: {}ms",
        properties.isCacheEnabled(), properties.getCacheExpireTimeMs());

    return compositeSource;
  }
}
