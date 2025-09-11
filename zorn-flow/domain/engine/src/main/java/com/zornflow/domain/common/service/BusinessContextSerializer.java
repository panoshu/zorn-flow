package com.zornflow.domain.common.service;

import com.zornflow.domain.common.valueobject.BusinessContext;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/30 15:27
 **/

public interface BusinessContextSerializer {
  String serialize(BusinessContext context);

  BusinessContext deserialize(String json);
}
