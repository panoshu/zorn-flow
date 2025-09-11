package com.zornflow.interfaces.web.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器。
 * 使用 @RestControllerAdvice 捕获所有 @RestController 抛出的异常，
 * 并将其转换为标准的API错误响应格式。
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * 处理由 @Valid 注解触发的验证错误。
   *
   * @param ex MethodArgumentNotValidException 异常实例
   * @return 包含所有字段验证错误的 400 Bad Request 响应
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });
    log.warn("请求体验证失败: {}", errors);
    return new ErrorResponse(
      HttpStatus.BAD_REQUEST.value(),
      "Validation Failed",
      "请求体验证失败，请检查提交的数据。",
      errors
    );
  }

  /**
   * 处理为了返回特定 HTTP 状态码而主动抛出的 ResponseStatusException。
   * 例如，在服务层找不到某个资源时 `throw new ResponseStatusException(HttpStatus.NOT_FOUND, "...")`
   *
   * @param ex ResponseStatusException 异常实例
   * @return 对应状态码和错误信息的响应
   */
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
    ErrorResponse errorResponse = new ErrorResponse(
      status.value(),
      status.getReasonPhrase(),
      ex.getReason(),
      null
    );
    log.warn("处理请求时发生已知错误: status={}, reason={}", status, ex.getReason());
    return new ResponseEntity<>(errorResponse, status);
  }

  /**
   * 处理所有其他未被捕获的服务器内部异常。
   *
   * @param ex Exception 异常实例
   * @return 统一的 500 Internal Server Error 响应
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse handleAllUncaughtException(Exception ex) {
    log.error("发生未捕获的服务器内部错误", ex);
    return new ErrorResponse(
      HttpStatus.INTERNAL_SERVER_ERROR.value(),
      "Internal Server Error",
      "服务器内部发生未知错误，请联系管理员。",
      null
    );
  }

  /**
   * 标准的 API 错误响应体。
   */
  public record ErrorResponse(
    int status,
    String error,
    String message,
    Map<String, String> details
  ) {
    private static final Instant timestamp = Instant.now();
  }
}
