package com.zornflow.domain.rule.valueobject;

import com.ddd.contract.valueobject.BaseValueObject;
import com.zornflow.domain.common.context.FlowContext;
import lombok.Getter;

import java.util.Objects;

/**
 * 规则执行结果值对象
 * 封装规则执行的结果信息
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/25 20:27
 */

@Getter
public final class RuleExecutionResult implements BaseValueObject {
  private final boolean success;
  private final FlowContext context;
  private final String message;
  private final Throwable error;

  /**
   * 创建成功的执行结果
   * @param context 执行后的业务上下文
   * @return 成功的执行结果
   */
  public static RuleExecutionResult success(FlowContext context) {
    return new RuleExecutionResult(true, context, null, null);
  }

  /**
   * 创建成功的执行结果（带消息）
   * @param context 执行后的业务上下文
   * @param message 成功消息
   * @return 成功的执行结果
   */
  public static RuleExecutionResult success(FlowContext context, String message) {
    return new RuleExecutionResult(true, context, message, null);
  }

  /**
   * 创建失败的执行结果
   * @param context 执行后的业务上下文
   * @param error 错误信息
   * @return 失败的执行结果
   */
  public static RuleExecutionResult failure(FlowContext context, Throwable error) {
    return new RuleExecutionResult(false, context, error.getMessage(), error);
  }

  /**
   * 创建失败的执行结果（带自定义消息）
   * @param context 执行后的业务上下文
   * @param message 错误消息
   * @param error 异常对象
   * @return 失败的执行结果
   */
  public static RuleExecutionResult failure(FlowContext context, String message, Throwable error) {
    return new RuleExecutionResult(false, context, message, error);
  }

  /**
   * 私有构造函数
   */
  private RuleExecutionResult(boolean success, FlowContext context, String message, Throwable error) {
    this.success = success;
    this.context = Objects.requireNonNull(context, "业务上下文不能为空");
    this.message = message;
    this.error = error;
  }

}
