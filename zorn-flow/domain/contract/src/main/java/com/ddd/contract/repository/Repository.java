package com.ddd.contract.repository;

import com.ddd.contract.aggregate.AggregateRoot;
import com.ddd.contract.valueobject.Identifier;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/24 16:32
 */
public sealed interface Repository<A extends AggregateRoot<ID>, ID extends Identifier>
  permits BaseRepository{
}
