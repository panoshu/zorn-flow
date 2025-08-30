package com.zornflow.infrastructure.presistence.mapper;

import com.zornflow.domain.common.service.BusinessContextSerializer;
import com.zornflow.domain.common.valueobject.BusinessContext;
import com.zornflow.domain.process.entity.ProcessInstance;
import com.zornflow.infrastructure.presistence.record.ProcessInstanceRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/30 15:15
 **/

@Mapper(componentModel = "spring")
public abstract class ProcessInstanceMapper {

  @Autowired
  private BusinessContextSerializer serializer;

  @Mapping(source = "id.value", target = "id")
  @Mapping(source = "processChainId.value", target = "processChainId")
  @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
  @Mapping(source = "currentNodeId.value", target = "currentNodeId")
  @Mapping(source = "context", target = "context", qualifiedByName = "contextToJson")
  @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "instantToLocalDateTime")
  @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "instantToLocalDateTime")
  @Mapping(source = "version", target = "version")
  public abstract ProcessInstanceRecord toDto(ProcessInstance processInstance);

  @Mapping(source = "id", target = "processInstanceId")
  @Mapping(source = "processChainId", target = "processChainId")
  @Mapping(source = "status", target = "status", qualifiedByName = "stringToStatus")
  @Mapping(source = "currentNodeId", target = "currentNodeId")
  @Mapping(source = "context", target = "initialContext", qualifiedByName = "jsonToContext")
  @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "localDateTimeToInstant")
  @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "localDateTimeToInstant")
  @Mapping(source = "version", target = "version")
  protected abstract ProcessInstance toDomainInternal(ProcessInstanceRecord dto);

  public ProcessInstance toDomain(ProcessInstanceRecord dto) {
    return toDomainInternal(dto);
  }

  @Named("contextToJson")
  String contextToJson(BusinessContext context) {
    return context.toJson(serializer);
  }

  @Named("jsonToContext")
  BusinessContext jsonToContext(String json) {
    return BusinessContext.fromJson(json, serializer);
  }

  @Named("statusToString")
  String statusToString(ProcessInstance.ProcessInstanceStatus status) {
    return status != null ? status.name() : null;
  }

  @Named("stringToStatus")
  ProcessInstance.ProcessInstanceStatus stringToStatus(String status) {
    return status != null ? ProcessInstance.ProcessInstanceStatus.valueOf(status) : null;
  }

  @Named("instantToLocalDateTime")
  LocalDateTime instantToLocalDateTime(Instant instant) {
    return instant != null ? LocalDateTime.ofInstant(instant, ZoneOffset.UTC) : null;
  }

  @Named("localDateTimeToInstant")
  Instant localDateTimeToInstant(LocalDateTime localDateTime) {
    return localDateTime != null ? localDateTime.atZone(ZoneOffset.UTC).toInstant() : null;
  }
}
