package com.zornflow.infrastructure.config.source.database;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 数据库配置类
 * 集成配置属性和 Bean 定义
 * 使用 Spring Boot 标准的 DataSource 自动配置
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
@Configuration
@ConfigurationProperties(prefix = "zornflow.db")
@ConditionalOnProperty(prefix = "zornflow.db", name = "enabled", havingValue = "true")
public class DatabaseConfiguration {

  /**
   * 是否启用数据库配置加载器
   * 数据源配置请使用 spring.datasource 标准配置
   */
  private boolean enabled = false;

  // Getters and Setters
  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * 配置JOOQ DSL上下文
   * 使用 Spring Boot 自动配置的 DataSource
   *
   * @param dataSource Spring Boot 自动配置的数据源
   * @return JOOQ DSLContext 实例
   */
  @Bean
  public DSLContext dslContext(DataSource dataSource) {
    return DSL.using(dataSource, SQLDialect.POSTGRES);
  }
}
