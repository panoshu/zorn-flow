package com.zornflow.infrastructure.repository;

import com.zornflow.domain.common.valueobject.BusinessContext;
import com.zornflow.domain.process.entity.ProcessInstance;
import com.zornflow.domain.process.types.ProcessChainId;
import com.zornflow.domain.process.types.ProcessInstanceId;
import com.zornflow.domain.process.types.ProcessNodeId;
import com.zornflow.infrastructure.persistence.jooq.tables.records.ProcessInstancesRecord;
import com.zornflow.infrastructure.mapper.ProcessInstanceMapper;
import com.zornflow.infrastructure.repository.ProcessInstanceRepositoryImpl;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.SelectWhereStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.zornflow.infrastructure.persistence.jooq.Tables.PROCESS_INSTANCES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessInstanceRepositoryImpl 单元测试")
public class ProcessInstanceRepositoryImplTest {

  @Mock
  private DSLContext dsl;

  @Mock
  private ProcessInstanceMapper mapper;

  // 1. 将 ProcessInstancesRecord 也声明为 Mock 对象
  @Mock
  private ProcessInstancesRecord record;

  @InjectMocks
  private ProcessInstanceRepositoryImpl repository;

  private ProcessInstance domainInstance;
  private ProcessInstanceId instanceId;

  @BeforeEach
  void setUp() {
    instanceId = ProcessInstanceId.generate();

    domainInstance = ProcessInstance.builder()
      .processInstanceId(instanceId)
      .processChainId(ProcessChainId.of("pc-1"))
      .status(ProcessInstance.ProcessInstanceStatus.RUNNING)
      .initialContext(new BusinessContext(Map.of("key", "value")))
      .startNodeId(ProcessNodeId.of("node-1"))
      .build();

    // 不再需要在这里 new record，因为它已经是 Mock 了
  }

  @Test
  @DisplayName("findById: 当记录存在时，应返回映射后的领域实体")
  @SuppressWarnings("unchecked") // 必须保留，因为 Mockito 对泛型的处理方式
  void findById_shouldReturnEntity_whenRecordExists() {
    // Arrange
    // 1. 为链式调用中的每一步都创建 Mock 对象，并指定正确的泛型
    var selectWhereStep = mock(SelectWhereStep.class);
    var selectConditionStep = mock(SelectConditionStep.class);

    // 2. 按照调用顺序，精确地安排（stub）每个方法的返回值
    // dsl.selectFrom(...) 返回 selectWhereStep
    when(dsl.selectFrom(PROCESS_INSTANCES)).thenReturn(selectWhereStep);
    // selectWhereStep.where(...) 返回 selectConditionStep
    when(selectWhereStep.where(any(org.jooq.Condition.class))).thenReturn(selectConditionStep);
    // selectConditionStep.fetchOptional() 返回最终的 record
    when(selectConditionStep.fetchOptional()).thenReturn(Optional.of(record));

    // 安排 mapper 的行为
    when(mapper.toDomain(record)).thenReturn(domainInstance);

    // Act
    Optional<ProcessInstance> result = repository.findById(instanceId);

    // Assert
    assertTrue(result.isPresent(), "结果应该是存在的");
    assertEquals(domainInstance, result.get(), "返回的实体应该和预期的实体相同");

    // 验证 jOOQ 的调用链
    verify(dsl).selectFrom(PROCESS_INSTANCES);
    verify(selectWhereStep).where(any(org.jooq.Condition.class));
    verify(selectConditionStep).fetchOptional();
  }

  @Test
  @DisplayName("findAll: 应返回所有实体")
  void findAll_shouldReturnAllEntities() {
    // Arrange
    @SuppressWarnings("unchecked")
    SelectWhereStep<ProcessInstancesRecord> selectStep = mock(SelectWhereStep.class);
    @SuppressWarnings("unchecked")
    Result<ProcessInstancesRecord> resultMock = mock(Result.class);

    when(dsl.selectFrom(PROCESS_INSTANCES)).thenReturn(selectStep);
    when(selectStep.fetch()).thenReturn(resultMock);
    when(resultMock.stream()).thenReturn(Stream.of(record));
    when(mapper.toDomain(record)).thenReturn(domainInstance);

    // Act
    List<ProcessInstance> results = repository.findAll();

    // Assert
    assertThat(results).hasSize(1);
    assertThat(results.getFirst()).isEqualTo(domainInstance);
  }


  @Test
  @DisplayName("save: 对于新实体，应创建新记录并调用 store()")
  void save_shouldCreateAndStoreNewRecord_forNewEntity() {
    // Arrange
    // 当查询时，返回空，表示是新实体
    when(dsl.fetchOne(eq(PROCESS_INSTANCES), any(org.jooq.Condition.class))).thenReturn(null);
    // 当创建新记录时，返回我们 Mock 的 record 对象
    when(dsl.newRecord(PROCESS_INSTANCES)).thenReturn(record);

    // Act
    repository.save(domainInstance);

    // Assert
    // 验证调用了 mapper 进行数据填充
    verify(mapper).updateRecord(eq(domainInstance), eq(record));
    // 验证设置了创建和更新时间戳
    verify(record).setCreatedAt(any(OffsetDateTime.class));
    verify(record).setUpdatedAt(any(OffsetDateTime.class));
    // 核心验证：验证 .store() 方法被调用了
    verify(record).store();
  }

  @Test
  @DisplayName("save: 对于已存在的实体，应更新记录并调用 store()")
  void save_shouldUpdateAndStoreExistingRecord() {
    // Arrange
    // 当查询时，返回我们 Mock 的 record 对象，表示实体已存在
    when(dsl.fetchOne(eq(PROCESS_INSTANCES), any(org.jooq.Condition.class))).thenReturn(record);

    // Act
    repository.save(domainInstance);

    // Assert
    // 验证没有创建新记录
    verify(dsl, never()).newRecord(PROCESS_INSTANCES);
    // 验证调用了 mapper 进行数据填充
    verify(mapper).updateRecord(domainInstance, record);
    // 验证只设置了更新时间戳，没有设置创建时间戳
    verify(record, never()).setCreatedAt(any(OffsetDateTime.class));
    verify(record).setUpdatedAt(any(OffsetDateTime.class));
    // 核心验证：验证 .store() 方法被调用了
    verify(record).store();
  }
}
