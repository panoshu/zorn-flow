package com.zornflow.infrastructure.repository;

import com.zornflow.domain.process.entity.ProcessInstance;
import com.zornflow.domain.process.repository.ProcessInstanceRepository;
import com.zornflow.domain.process.types.ProcessInstanceId;
import com.zornflow.infrastructure.mapper.ProcessInstanceMapper;
import com.zornflow.infrastructure.persistence.jooq.tables.records.ProcessInstancesRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.zornflow.infrastructure.persistence.jooq.Tables.PROCESS_INSTANCES;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/7 19:34
 **/

@Repository
@RequiredArgsConstructor
public class ProcessInstanceRepositoryImpl implements ProcessInstanceRepository {

  private final DSLContext dsl;
  private final ProcessInstanceMapper mapper; // The mapper now handles everything

  @Override
  @Transactional(readOnly = true)
  public Optional<ProcessInstance> findById(ProcessInstanceId processInstanceId) {
    return dsl.selectFrom(PROCESS_INSTANCES)
      .where(PROCESS_INSTANCES.ID.eq(processInstanceId.value()))
      .fetchOptional()
      .map(mapper::toDomain); // Simple, clean delegation
  }

  @Override
  @Transactional(readOnly = true)
  public List<ProcessInstance> findAll() {
    return dsl.selectFrom(PROCESS_INSTANCES)
      .fetch()
      .stream()
      .map(mapper::toDomain)
      .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public ProcessInstance save(ProcessInstance processInstance) {
    ProcessInstancesRecord record = dsl.fetchOne(PROCESS_INSTANCES, PROCESS_INSTANCES.ID.eq(processInstance.getId().value()));

    if (record == null) {
      record = dsl.newRecord(PROCESS_INSTANCES);
      record.setCreatedAt(OffsetDateTime.now());
    }

    // Delegate the entire update logic to the mapper
    mapper.updateRecord(processInstance, record);

    record.setUpdatedAt(OffsetDateTime.now());
    record.store();

    return processInstance;
  }

  @Override
  @Transactional
  public void delete(ProcessInstance processInstance) {
    deleteById(processInstance.getId());
  }

  @Override
  @Transactional
  public void deleteById(ProcessInstanceId processInstanceId) {
    dsl.deleteFrom(PROCESS_INSTANCES)
      .where(PROCESS_INSTANCES.ID.eq(processInstanceId.value()))
      .execute();
  }
}
