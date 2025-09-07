package com.domain.contract.exception;

import java.util.Optional;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/24 15:59
 */
public abstract class BaseException extends RuntimeException {
  private final String code;
  private final Object[] messageArgs;

  public BaseException(ErrorInfo errorInfo, Object... messageArgs) {
    super(errorInfo.getMessage());
    this.code = errorInfo.getCode();
    this.messageArgs = messageArgs;
  }

  public BaseException(ErrorInfo errorInfo, Throwable cause, Object... messageArgs) {
    super(errorInfo.getMessage(), cause);
    this.code = errorInfo.getCode();
    this.messageArgs = messageArgs;
  }

  public BaseException(String code, String message, Object... messageArgs) {
    super(message);
    this.code = code;
    this.messageArgs = messageArgs;
  }

  @Override
  public String getMessage() {
    return Optional.ofNullable(messageArgs)
      .filter(args -> args.length > 0)
      .map(args -> String.format(super.getMessage(), args))
      .orElseGet(super::getMessage);
  }

  public String getCode() {
    return code;
  }

}
