package com.domain.contract.aggregate;


import com.domain.contract.valueobject.Identifier;

import java.time.Instant;
import java.util.Objects;

/**
 * 实体基类
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/6/1 14:15
 **/

public abstract class Entity<ID extends Identifier> {
  private final ID id;
  private final Instant createdAt;
  private Instant updatedAt;
  private Long version;

  protected Entity(ID id) {
    if (id == null) {
      throw new IllegalArgumentException("Entity ID cannot be null.");
    }
    this.id = id;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
    this.version = 0L;
  }

  // 通常在更新操作时调用，更新updatedAt并增加版本
  protected void markUpdated() {
    this.updatedAt = Instant.now();
    this.version++;
  }

  // 实体不变性校验方法 (抽象方法，强制子类实现)
  protected abstract void validateInvariants();

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    return o instanceof Entity<?> that && Objects.equals(this.id, that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  public ID getId() {
    return this.id;
  }
}
