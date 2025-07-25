package com.ddd.contract.aggregate;

import com.ddd.contract.valueobject.BaseValueObject;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/25 9:15
 */
public sealed interface BusinessContext extends BaseValueObject
  permits BaseBusinessContext{
}
