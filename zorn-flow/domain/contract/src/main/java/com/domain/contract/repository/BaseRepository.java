package com.domain.contract.repository;

import com.domain.contract.aggregate.AggregateRoot;
import com.domain.contract.valueobject.EntityId;

import java.util.Collection;
import java.util.Optional;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/24 16:30
 */

public non-sealed interface BaseRepository<A extends AggregateRoot<ID>, ID extends EntityId> extends Repository<A, ID> {

  Optional<A> findById(ID id);

  Collection<A> findAll();

  A save(A aggregateRoot);

  void delete(A aggregateRoot);

  void deleteById(ID id);

  // 还可以添加分页、排序等通用查询方法
  // Page<A> findAll(Pageable pageable);
  // List<A> findByIds(List<ID> ids);
}
