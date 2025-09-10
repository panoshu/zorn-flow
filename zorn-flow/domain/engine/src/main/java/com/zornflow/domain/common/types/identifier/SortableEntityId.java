package com.zornflow.domain.common.types.identifier;

import com.domain.contract.valueobject.EntityId;

/**
 * 可排序的实体ID，适用于有业务顺序意义的ID（如时间序ULID、SnowflakeId）
 */
public interface SortableEntityId<T extends SortableEntityId<T>> extends EntityId, Comparable<T> {
  @Override
  String value();
}
