package com.zornflow.domain.common.types;

import com.domain.contract.valueobject.EntityId;
import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import lombok.NonNull;

import java.time.Instant;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/25 12:39
 */
public record UlidEntityId(String value) implements EntityId {

  public UlidEntityId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("ULID identifier value cannot be null or empty.");
    }

    if (!Ulid.isValid(value)) {
      throw new IllegalArgumentException("Invalid ULID: " + value);
    }
  }

  public static UlidEntityId generate() {
    return new UlidEntityId(UlidCreator.getUlid().toString());
  }

  public static UlidEntityId of(@NonNull String value) {
    return new UlidEntityId(value);
  }

  public Instant getInstant() {
    return Ulid.getInstant(value);
  }
}
