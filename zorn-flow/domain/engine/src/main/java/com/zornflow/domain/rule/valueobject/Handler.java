package com.zornflow.domain.rule.valueobject;

import com.zornflow.domain.rule.types.HandlerType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/25 21:58
 **/

public record Handler(
  HandlerType type,
  String handler,
  Map<String, Object> parameters
) {
  public Handler(HandlerType type, String handler, Map<String, Object> parameters) {
    this.type = Objects.requireNonNull(type, "Handler type must not be null");
    this.handler = Objects.requireNonNull(handler, "Handler must not be null");
    this.parameters = new HashMap<>(parameters != null ? parameters : Map.of());
  }

  public static Handler of(HandlerType type, String handler) {
    return new Handler(type, handler, null);
  }

  public static Handler of(HandlerType type, String handler, Map<String, Object> parameters) {
    return new Handler(type, handler, parameters);
  }

  public Object getParameter(String key) {
    return parameters.get(key);
  }

  public Handler withParameter(String key, Object value) {
    Map<String, Object> newParameters = new HashMap<>(this.parameters);
    newParameters.put(key, value);
    return new Handler(this.type, this.handler, newParameters);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Handler that = (Handler) o;
    return type == that.type &&
      handler.equals(that.handler) &&
      parameters.equals(that.parameters);
  }

}
