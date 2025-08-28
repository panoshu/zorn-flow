package com.zornflow.domain.common.types;

import com.domain.contract.valueobject.Identifier;
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
public record UlidIdentifier(String value) implements Identifier {

  public UlidIdentifier {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("ULID identifier value cannot be null or empty.");
    }

    if (!Ulid.isValid(value)) {
      throw new IllegalArgumentException("Invalid ULID: " + value);
    }
  }

  public static UlidIdentifier generate() {
    return new UlidIdentifier(UlidCreator.getUlid().toString());
  }

  public static UlidIdentifier of(@NonNull String value) {
    return new UlidIdentifier(value);
  }

  public Instant getInstant() {
    return Ulid.getInstant(value);
  }
}
