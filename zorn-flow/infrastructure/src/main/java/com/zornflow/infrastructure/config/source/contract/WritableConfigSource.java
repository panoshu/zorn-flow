package com.zornflow.infrastructure.config.source.contract;

import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.model.RuleChainConfig;

import java.util.Collection;

/**
 * 可写配置源接口
 * 定义配置写入的统一抽象，支持可读写配置源实现
 * <p>
 * 遵循接口隔离原则：
 * - 继承ReadableConfigSource以支持读取操作
 * - 添加写入、更新、删除相关的操作
 * - 适用于支持写入的配置源（如数据库配置、配置中心等）
 * <p>
 * 设计原则：
 * - 写入操作具有事务性，要么全部成功要么全部失败
 * - 支持批量操作以提高性能
 * - 提供回滚机制以保证数据一致性
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
public interface WritableConfigSource extends ReadableConfigSource {

  // ============================= 规则链写入操作 =============================

  /**
   * 保存或更新规则链配置
   * 如果规则链已存在，则更新；否则创建新的规则链
   *
   * @param config 规则链配置
   * @return 操作结果
   * @throws ConfigSourceException 当保存失败时抛出异常
   */
  ConfigOperationResult saveRuleChain(RuleChainConfig config);

  /**
   * 批量保存或更新规则链配置
   *
   * @param configs 规则链配置集合
   * @return 批量操作结果
   * @throws ConfigSourceException 当批量保存失败时抛出异常
   */
  default BatchOperationResult saveRuleChains(Collection<RuleChainConfig> configs) {
    var results = configs.stream()
      .map(this::saveRuleChain)
      .toList();

    long successCount = results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
    return new BatchOperationResult(
      successCount == configs.size(),
      successCount,
      configs.size(),
      "Batch save rule chains: " + successCount + "/" + configs.size() + " succeeded"
    );
  }

  /**
   * 删除规则链配置（软删除）
   *
   * @param chainId 规则链ID
   * @return 操作结果
   * @throws ConfigSourceException 当删除失败时抛出异常
   */
  ConfigOperationResult deleteRuleChain(String chainId);

  // ============================= 流程链写入操作 =============================

  /**
   * 保存或更新流程链配置
   * 如果流程链已存在，则更新；否则创建新的流程链
   *
   * @param config 流程链配置
   * @return 操作结果
   * @throws ConfigSourceException 当保存失败时抛出异常
   */
  ConfigOperationResult saveProcessChain(ProcessChainConfig config);

  /**
   * 批量保存或更新流程链配置
   *
   * @param configs 流程链配置集合
   * @return 批量操作结果
   * @throws ConfigSourceException 当批量保存失败时抛出异常
   */
  default BatchOperationResult saveProcessChains(Collection<ProcessChainConfig> configs) {
    var results = configs.stream()
      .map(this::saveProcessChain)
      .toList();

    long successCount = results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
    return new BatchOperationResult(
      successCount == configs.size(),
      successCount,
      configs.size(),
      "Batch save process chains: " + successCount + "/" + configs.size() + " succeeded"
    );
  }

  /**
   * 删除流程链配置（软删除）
   *
   * @param chainId 流程链ID
   * @return 操作结果
   * @throws ConfigSourceException 当删除失败时抛出异常
   */
  ConfigOperationResult deleteProcessChain(String chainId);

  // ============================= 事务操作 =============================

  /**
   * 检查配置源是否支持事务操作
   *
   * @return 如果支持事务返回true，否则返回false
   */
  default boolean supportsTransaction() {
    return false;
  }

  /**
   * 在事务中执行操作
   * 只有当supportsTransaction()返回true时才能使用
   *
   * @param operation 要执行的操作
   * @param <T>       操作结果类型
   * @return 操作结果
   * @throws ConfigSourceException         当事务操作失败时抛出异常
   * @throws UnsupportedOperationException 当配置源不支持事务时抛出异常
   */
  default <T> T executeInTransaction(TransactionalOperation<T> operation) {
    throw new UnsupportedOperationException("Transaction not supported by this config source");
  }

  // ============================= 结果类型定义 =============================

  /**
   * 事务操作接口
   */
  @FunctionalInterface
  interface TransactionalOperation<T> {
    T execute() throws Exception;
  }

  /**
   * 配置操作结果
   */
  record ConfigOperationResult(
    boolean isSuccess,
    String message,
    String chainId
  ) {
  }

  /**
   * 批量操作结果
   */
  record BatchOperationResult(
    boolean isSuccess,
    long successCount,
    long totalCount,
    String message
  ) {
  }

  /**
   * 配置源异常
   */
  class ConfigSourceException extends RuntimeException {
    public ConfigSourceException(String message) {
      super(message);
    }

    public ConfigSourceException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
