package com.zornflow.infrastructure.config.source;

/**
 * 读写配置源接口
 * 同时支持读取和写入操作的配置源，如数据库、远程配置中心等
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
public interface ReadWriteConfigSource extends ReadableConfigSource, WriteableConfigSource {

  /**
   * 获取配置源优先级
   * 数值越小优先级越高
   *
   * @return 优先级数值
   */
  int getPriority();

  /**
   * 检查配置源是否可用
   *
   * @return true如果配置源可用
   */
  boolean isAvailable();

  /**
   * 刷新配置源
   * 重新加载配置数据
   *
   * @return
   */
  boolean refresh();

  /**
   * 清空缓存（如果有的话）
   */
  void clearCache();
}
