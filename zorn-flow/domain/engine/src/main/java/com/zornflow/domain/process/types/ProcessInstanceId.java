package com.zornflow.domain.process.types;

import com.domain.contract.valueobject.DomainPrimitive;
import com.domain.contract.valueobject.Identifier;
import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import lombok.NonNull;

import java.time.Instant;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/25 22:30
 **/

public record ProcessInstanceId(String value) implements DomainPrimitive, Identifier {
  public ProcessInstanceId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("ULID identifier value cannot be null or empty.");
    }

    if (!Ulid.isValid(value)) {
      throw new IllegalArgumentException("Invalid ULID: " + value);
    }
  }

  public static ProcessInstanceId generate() {
    return new ProcessInstanceId(UlidCreator.getUlid().toString());
  }

  public static ProcessInstanceId of(@NonNull String value) {
    return new ProcessInstanceId(value);
  }

  public Instant getInstant() {
    return Ulid.getInstant(value);
  }
}
