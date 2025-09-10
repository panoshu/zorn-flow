package com.domain.contract.event;

import com.domain.contract.valueobject.EntityId;

/**
 * DDD 领域事件 (Domain Event) 根契约
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/24 11:40
 */
public sealed interface DomainEvent<ID extends EntityId>
  permits BaseDomainEvent {
}
