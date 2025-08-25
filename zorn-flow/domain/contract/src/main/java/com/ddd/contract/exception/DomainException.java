package com.ddd.contract.exception;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/24 16:03
 */
public class DomainException extends BaseException {
  public DomainException(ErrorInfo errorInfo, Object... messageArgs) {
    super(errorInfo, messageArgs);
  }

  public DomainException(ErrorInfo errorInfo, String message, Throwable cause, Object... messageArgs) {
    super(errorInfo, cause, messageArgs);
  }
}
