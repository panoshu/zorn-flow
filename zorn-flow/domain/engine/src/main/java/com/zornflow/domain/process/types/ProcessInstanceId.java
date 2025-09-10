package com.zornflow.domain.process.types;

import com.domain.contract.valueobject.DomainPrimitive;
import com.domain.contract.valueobject.EntityId;
import com.zornflow.domain.common.types.identifier.DomainIds;
import lombok.NonNull;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/25 22:30
 **/

public record ProcessInstanceId(String value) implements EntityId {
  public ProcessInstanceId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("ULID identifier value cannot be null or empty.");
    }
  }

  public static ProcessInstanceId generate() {
    return DomainIds.next(ProcessInstanceId.class, ProcessInstanceId::new);
  }

  public static ProcessInstanceId of(@NonNull String raw) {
    return DomainIds.of(raw, ProcessInstanceId.class, ProcessInstanceId::new);
  }
}
