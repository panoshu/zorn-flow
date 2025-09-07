package com.zornflow.infrastructure.mapper;

import com.zornflow.domain.common.service.BusinessContextSerializer;
import com.zornflow.domain.common.valueobject.BusinessContext;
import com.zornflow.domain.process.entity.ProcessInstance;
import com.zornflow.domain.process.types.ProcessChainId;
import com.zornflow.domain.process.types.ProcessInstanceId;
import com.zornflow.domain.process.types.ProcessNodeId;
import com.zornflow.infrastructure.persistence.jooq.tables.records.ProcessInstancesRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.JSONB;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

/**
 * MapStruct Mapper for converting between ProcessInstance domain entities
 * and jOOQ ProcessInstancesRecord objects.
 */
@Component
@RequiredArgsConstructor
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {CommonTypeMapper.class})
public abstract class ProcessInstanceMapper {

  protected BusinessContextSerializer contextSerializer;

  @Mapping(target = "processInstanceId", source = "id", qualifiedByName = "stringToProcessInstanceId")
  @Mapping(target = "processChainId", source = "processChainId", qualifiedByName = "stringToProcessChainId")
  @Mapping(target = "startNodeId", source = "currentNodeId", qualifiedByName = "stringToProcessNodeId")
  @Mapping(target = "status", expression = "java(com.zornflow.domain.process.entity.ProcessInstance.ProcessInstanceStatus.valueOf(record.getStatus()))")
  @Mapping(target = "initialContext", source = "context", qualifiedByName = "jsonbToContext")
  public abstract ProcessInstance toDomain(ProcessInstancesRecord record);

  @Mapping(target = "id", source = "id", qualifiedByName = "processInstanceIdToString")
  @Mapping(target = "processChainId", source = "processChainId", qualifiedByName = "processChainIdToString")
  @Mapping(target = "currentNodeId", source = "currentNodeId", qualifiedByName = "processNodeIdToString")
  @Mapping(target = "status", expression = "java(entity.getStatus().name())")
  @Mapping(target = "context", source = "context", qualifiedByName = "contextToJsonb")
  @Mapping(target = "version", source = "version")
  public abstract void updateRecord(ProcessInstance entity, @MappingTarget ProcessInstancesRecord record);

  @Named("jsonbToContext")
  protected BusinessContext jsonbToContext(JSONB jsonb) {
    if (jsonb == null) {
      return null;
    }
    return contextSerializer.deserialize(jsonb.data());
  }

  @Named("contextToJsonb")
  protected JSONB contextToJsonb(BusinessContext context) {
    if (context == null) {
      return null;
    }
    return JSONB.valueOf(contextSerializer.serialize(context));
  }

  // --- Value Object Converters ---

  @Named("stringToProcessInstanceId")
  ProcessInstanceId stringToProcessInstanceId(String id) {
    return id != null ? ProcessInstanceId.of(id) : null;
  }

  @Named("processInstanceIdToString")
  String processInstanceIdToString(ProcessInstanceId id) {
    return id != null ? id.value() : null;
  }

  @Named("stringToProcessChainId")
  ProcessChainId stringToProcessChainId(String id) {
    return id != null ? ProcessChainId.of(id) : null;
  }

  @Named("processChainIdToString")
  String processChainIdToString(ProcessChainId id) {
    return id != null ? id.value() : null;
  }

  @Named("stringToProcessNodeId")
  ProcessNodeId stringToProcessNodeId(String id) {
    return id != null ? ProcessNodeId.of(id) : null;
  }

  @Named("processNodeIdToString")
  String processNodeIdToString(ProcessNodeId id) {
    return id != null ? id.value() : null;
  }
}
