package com.zornflow.domain.rule.valueobject;

import com.ddd.contract.valueobject.BaseValueObject;
import com.zornflow.domain.common.context.FlowContext;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 规则链执行结果值对象
 * 封装规则链执行的整体结果
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/31 16:06
 */

@Getter
public final class RuleChainExecutionResult implements BaseValueObject {
  private final boolean success;
  private final FlowContext finalContext;
  private final List<RuleExecutionResult> ruleResults;
  private final String message;
  private final Throwable error;

  /**
   * 创建成功的规则链执行结果
   * @param finalContext 最终业务上下文
   * @param ruleResults 各规则执行结果
   * @return 规则链执行结果
   */
  public static RuleChainExecutionResult success(FlowContext finalContext, List<RuleExecutionResult> ruleResults) {
    return new RuleChainExecutionResult(true, finalContext, ruleResults, null, null);
  }

  /**
   * 创建失败的规则链执行结果
   * @param finalContext 最终业务上下文
   * @param ruleResults 各规则执行结果
   * @param error 错误信息
   * @return 规则链执行结果
   */
  public static RuleChainExecutionResult failure(FlowContext finalContext, List<RuleExecutionResult> ruleResults, Throwable error) {
    return new RuleChainExecutionResult(false, finalContext, ruleResults, error.getMessage(), error);
  }

  /**
   * 私有构造函数
   */
  private RuleChainExecutionResult(boolean success, FlowContext finalContext, List<RuleExecutionResult> ruleResults,
                                   String message, Throwable error) {
    this.success = success;
    this.finalContext = Objects.requireNonNull(finalContext, "最终业务上下文不能为空");
    this.ruleResults = ruleResults != null ? Collections.unmodifiableList(ruleResults) : Collections.emptyList();
    this.message = message;
    this.error = error;
  }

  /**
   * 获取第一个失败的规则执行结果
   * @return 失败的规则执行结果，如果没有则返回null
   */
  public RuleExecutionResult getFirstFailure() {
    return ruleResults.stream()
      .filter(result -> !result.isSuccess())
      .findFirst()
      .orElse(null);
  }

  /**
   * 判断是否有规则执行失败
   * @return true表示有规则执行失败
   */
  public boolean hasFailures() {
    return ruleResults.stream().anyMatch(result -> !result.isSuccess());
  }
}
