package com.domain.contract.event;


import com.domain.contract.valueobject.Identifier;

import java.time.Instant;

/**
 * 可供外部实现的、非密封的领域事件基础接口
 *
 * <p>
 * 所有具体的领域事件都应实现这个接口
 * 本接口提供了所有事件都应具备的核心属性: 事件ID和发生事件
 * 建议使用 Record 来实现
 * </p>
 * <p>
 * <b>使用示例: </b>
 * <br/>
 * {@code public record OrderPlaceEvent(OrderId orderId) implements BaseDomainEvent {}}
 * </p>
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/24 11:42
 */
public non-sealed interface BaseDomainEvent<ID extends Identifier> extends DomainEvent<ID> {
  Instant occurredOn = Instant.now();

  ID eventId();
  Instant occurredOn();
}
