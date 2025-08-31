package com.zornflow.infrastructure.config.mapper;

import com.zornflow.domain.common.types.Version;
import org.mapstruct.Named;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/31 2:29
 */
public class CommonMappings {
  @Named("stringToVersion")
  public static Version stringToVersion(String version) {
    if (version == null || version.isBlank()) {
      return Version.of("1.0.0");
    }
    // 如果版本号不符合 x.y.z 格式，自动补充
    String[] parts = version.split("\\.");
    if (parts.length == 2) {
      return Version.of(version + ".0");
    } else if (parts.length == 1) {
      return Version.of(version + ".0.0");
    }
    return Version.of(version);
  }
}
