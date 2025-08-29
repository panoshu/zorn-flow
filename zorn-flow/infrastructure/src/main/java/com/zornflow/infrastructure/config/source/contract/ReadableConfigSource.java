package com.zornflow.infrastructure.config.source.contract;

import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.model.RuleChainConfig;

import java.util.List;
import java.util.Optional;

/**
 * 可读配置源接口
 * 定义配置读取的统一抽象，支持只读配置源实现
 * <p>
 * 遵循接口隔离原则：
 * - 只定义读取相关的操作
 * - 适用于不支持写入的配置源（如类路径配置、只读文件配置等）
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
public interface ReadableConfigSource extends ConfigSource {

  // ============================= 规则链查询 =============================

  /**
   * 根据ID查找规则链配置
   *
   * @param chainId 规则链ID
   * @return 规则链配置的Optional包装
   */
  Optional<RuleChainConfig> findRuleChain(String chainId);

  /**
   * 获取所有规则链配置
   *
   * @return 规则链配置列表
   */
  List<RuleChainConfig> findAllRuleChains();

  /**
   * 根据版本查找规则链配置
   *
   * @param chainId 规则链ID
   * @param version 版本号
   * @return 规则链配置的Optional包装
   */
  default Optional<RuleChainConfig> findRuleChainByVersion(String chainId, String version) {
    return findRuleChain(chainId)
      .filter(config -> version.equals(config.version()));
  }

  // ============================= 流程链查询 =============================

  /**
   * 根据ID查找流程链配置
   *
   * @param chainId 流程链ID
   * @return 流程链配置的Optional包装
   */
  Optional<ProcessChainConfig> findProcessChain(String chainId);

  /**
   * 获取所有流程链配置
   *
   * @return 流程链配置列表
   */
  List<ProcessChainConfig> findAllProcessChains();

  /**
   * 根据版本查找流程链配置
   *
   * @param chainId 流程链ID
   * @param version 版本号
   * @return 流程链配置的Optional包装
   */
  default Optional<ProcessChainConfig> findProcessChainByVersion(String chainId, String version) {
    return findProcessChain(chainId)
      .filter(config -> version.equals(config.version()));
  }

  // ============================= 缓存管理 =============================

  /**
   * 刷新配置缓存
   * 重新加载配置源，适用于支持热更新的只读配置源
   */
  default void refresh() {
    // 默认实现为空，子类可以根据需要重写
  }

  /**
   * 检查配置是否存在
   *
   * @param chainId     配置链ID
   * @param isRuleChain 是否为规则链，true表示规则链，false表示流程链
   * @return 如果配置存在返回true，否则返回false
   */
  default boolean exists(String chainId, boolean isRuleChain) {
    if (isRuleChain) {
      return findRuleChain(chainId).isPresent();
    } else {
      return findProcessChain(chainId).isPresent();
    }
  }
}
