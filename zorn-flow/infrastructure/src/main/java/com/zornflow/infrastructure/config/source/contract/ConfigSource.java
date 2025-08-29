package com.zornflow.infrastructure.config.source.contract;

/**
 * 配置源接口
 * 定义配置加载的统一抽象，支持多种配置源实现
 * <p>
 * 遵循开闭原则：
 * - 对扩展开放：可以轻松添加新的配置源实现
 * - 对修改关闭：不需要修改现有代码即可支持新配置源
 * <p>
 * 设计原则：
 * - 单一职责：每个配置源只负责自己的配置管理
 * - 依赖倒置：上层模块依赖抽象而非具体实现
 * - 接口隔离：分离读取和写入操作
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
public interface ConfigSource {

  // ============================= 基本属性 =============================

  /**
   * 获取配置源名称
   *
   * @return 配置源名称，如 "classpath", "database", "nacos"
   */
  String getSourceName();

  /**
   * 获取配置源优先级
   * 数值越小优先级越高，用于多配置源合并时的优先级判断
   * <p>
   * 推荐优先级：
   * - 10: 外部配置中心 (如Nacos)
   * - 20: 数据库配置
   * - 30: 环境变量配置
   * - 100: 本地文件配置 (如YAML)
   *
   * @return 优先级值
   */
  int getPriority();

  /**
   * 检查配置源是否可用
   *
   * @return 如果配置源可用返回true，否则返回false
   */
  boolean isAvailable();

  /**
   * 获取配置源统计信息
   *
   * @return 统计信息
   */
  ConfigSourceStatistics getStatistics();

  /**
   * 配置源统计信息
   */
  record ConfigSourceStatistics(
    String sourceName,
    int ruleChainCount,
    int processChainCount,
    boolean isAvailable,
    String additionalInfo
  ) {
  }
}
