package com.zornflow.domain.rule.service;

import com.zornflow.domain.common.valueobject.BusinessContext;
import com.zornflow.domain.rule.valueobject.Handler;

/**
 * 定义处理器执行的抽象，隔离外部实现
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/1 9:47
 */

public interface HandlerExecutor {

  /**
   * 根据规则配置中的处理器信息执行规则处理
   *
   * @param handler 规则配置
   * @param context 执行上下文
   */
  void execute(Handler handler, BusinessContext context);

  /**
   * 检查是否支持该处理器类型
   *
   * @param handler 处理器类型
   * @return 是否支持
   */
  boolean supports(Handler handler);
}
