package com.ddd.contract.aggregate;

import com.ddd.contract.event.DomainEvent;
import com.ddd.contract.identifier.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/24 12:34
 */
public abstract class AggregateRoot<ID extends Identifier> extends Entity<ID> {

  private final transient List<DomainEvent<ID>> domainEvents = new ArrayList<>();

  protected AggregateRoot(ID id) {
    super(id);
  }

  /**
   * 注册一个领域事件。
   * 聚合根在业务操作中生成事件，然后通过此方法注册。
   */
  protected void registerDomainEvent(DomainEvent<ID> event) {
    this.domainEvents.add(Objects.requireNonNull(event, "Domain event cannot be null"));
  }

  /**
   * 获取所有已注册的领域事件。
   * 通常在聚合根保存后被清理。
   */
  public List<DomainEvent<ID>> getDomainEvents() {
    return Collections.unmodifiableList(domainEvents);
  }

  /**
   * 清理所有已注册的领域事件。
   * 通常在领域事件发布后调用。
   */
  public void clearDomainEvents() {
    this.domainEvents.clear();
  }
}
