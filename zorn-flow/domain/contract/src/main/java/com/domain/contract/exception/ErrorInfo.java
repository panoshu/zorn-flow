package com.domain.contract.exception;

/**
 * 领域错误码接口, 用于定义业务相关的错误码
 * 各个领域的具体错误码枚举应实现此接口
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/5/31 22:56
 **/

public interface ErrorInfo {
  String getCode();

  String getMessage();
}
