package com.domain.contract.valueobject;

import java.io.Serializable;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/25 21:34
 **/

public interface EntityId extends DomainPrimitive, Serializable {
  String value();
}
