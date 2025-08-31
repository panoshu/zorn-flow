package com.zornflow.infrastructure.config.mapper;

import com.zornflow.domain.common.types.Version;
import org.mapstruct.MapperConfig;
import org.mapstruct.Named;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/31 2:25
 */
@MapperConfig(
  componentModel = "spring",
  uses = {CommonMappings.class}
)
public interface MapstructConfig {

}
