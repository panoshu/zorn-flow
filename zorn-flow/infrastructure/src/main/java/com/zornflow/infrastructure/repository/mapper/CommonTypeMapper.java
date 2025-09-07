package com.zornflow.infrastructure.repository.mapper;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * A central MapStruct mapper for common data type conversions.
 * This component is used by other mappers to avoid code duplication.
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/7 11:31
 **/
@Component
public class CommonTypeMapper {
  /**
   * Converts a java.time.Instant to a java.time.OffsetDateTime at UTC.
   *
   * @param instant the source Instant
   * @return the resulting OffsetDateTime, or null if the source is null
   */
  public OffsetDateTime instantToOffsetDateTime(Instant instant) {
    if (instant == null) {
      return null;
    }
    return instant.atOffset(ZoneOffset.UTC);
  }

  /**
   * Converts a java.time.OffsetDateTime to a java.time.Instant.
   *
   * @param offsetDateTime the source OffsetDateTime
   * @return the resulting Instant, or null if the source is null
   */
  public Instant offsetDateTimeToInstant(OffsetDateTime offsetDateTime) {
    if (offsetDateTime == null) {
      return null;
    }
    return offsetDateTime.toInstant();
  }
}
