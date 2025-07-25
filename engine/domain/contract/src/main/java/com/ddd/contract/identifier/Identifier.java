package com.ddd.contract.identifier;

import java.io.Serializable;

/**
 * DDD 标识符 (ID) 根契约
 *
 * <p>
 * 作为统一类型约束，用于框架层面对 ID 的通用处理
 * 密封接口, 提供一个 non-sealed 接口 BaseIdentifier 来为外部实现提供安全的扩展点
 * </p>
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/7/24 11:07
 **/

public sealed interface Identifier extends Serializable
  permits BaseIdentifier {
}
