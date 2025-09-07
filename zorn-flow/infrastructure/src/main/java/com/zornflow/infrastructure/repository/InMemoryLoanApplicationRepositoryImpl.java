package com.zornflow.infrastructure.persistence.repository;

import com.zornflow.domain.loan.model.LoanApplication;
import com.zornflow.domain.loan.model.LoanApplicationId;
import com.zornflow.domain.loan.repository.LoanApplicationRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LoanApplicationRepository 的内存模拟实现。
 * 用于开发和测试阶段，不依赖数据库。
 * 注意：这是一个简单的模拟，并未实现事务性。
 */
@Repository // 标记为 Spring Bean，以便可以被注入
public class InMemoryLoanApplicationRepositoryImpl implements LoanApplicationRepository {

  // 使用 ConcurrentHashMap 作为线程安全的内存数据库
  private final Map<LoanApplicationId, LoanApplication> database = new ConcurrentHashMap<>();

  @Override
  public Optional<LoanApplication> findById(LoanApplicationId id) {
    // an clone to simulate fetching from a real database
    return Optional.ofNullable(database.get(id));
  }

  @Override
  public Collection<LoanApplication> findAll() {
    return new ArrayList<>(database.values());
  }

  @Override
  public LoanApplication save(LoanApplication aggregateRoot) {
    // 模拟 INSERT 或 UPDATE 操作
    database.put(aggregateRoot.getId(), aggregateRoot);
    return aggregateRoot;
  }

  @Override
  public void delete(LoanApplication aggregateRoot) {
    database.remove(aggregateRoot.getId());
  }

  @Override
  public void deleteById(LoanApplicationId id) {
    database.remove(id);
  }
}
