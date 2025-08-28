package com.zornflow.domain.rule.valueobject;

import com.domain.contract.valueobject.BaseValueObject;
import com.zornflow.domain.rule.types.HandlerType;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * 规则动作值对象
 * 封装规则的动作信息，包括类型、处理器和参数
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/25 19:34
 */

public record Action(
  HandlerType type,
  String handler,
  Map<String, Object> parameters
) implements BaseValueObject {

  public Action {
    Objects.requireNonNull(type, "动作类型不能为空");
    Objects.requireNonNull(handler, "处理器标识不能为空");
    parameters = parameters != null ? Collections.unmodifiableMap(parameters) : Collections.emptyMap();
  }

  /**
   * 创建动作对象
   * @param type 动作类型
   * @param handler 处理器标识
   * @param parameters 参数映射
   * @return 动作对象
   */
  public static Action of(HandlerType type, String handler, Map<String, Object> parameters) {
    return new Action(type, handler, parameters);
  }

  /**
   * 创建无参数的动作对象
   * @param type 动作类型
   * @param handler 处理器标识
   * @return 动作对象
   */
  public static Action of(HandlerType type, String handler) {
    return new Action(type, handler, Collections.emptyMap());
  }

  /**
   * 判断是否包含指定参数
   * @param paramName 参数名称
   * @return true表示包含该参数
   */
  public boolean hasParameter(String paramName) {
    return parameters.containsKey(paramName);
  }

  /**
   * 获取参数值
   * @param paramName 参数名称
   * @return 参数值，如果不存在返回null
   */
  public Object getParameter(String paramName) {
    return parameters.get(paramName);
  }
}
