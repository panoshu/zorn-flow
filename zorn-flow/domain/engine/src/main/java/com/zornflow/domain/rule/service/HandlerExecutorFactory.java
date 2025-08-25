package com.zornflow.domain.rule.service;

import com.zornflow.domain.rule.valueobject.HandlerConfig;

import java.util.Optional;

/**
 * Handler执行器工厂/解析器接口
 * 负责根据Handler的类型找到对应的执行器
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/25 22:59
 **/

public interface HandlerExecutorFactory {
  Optional<HandlerExecutor> getExecutor(HandlerConfig handler);
}
