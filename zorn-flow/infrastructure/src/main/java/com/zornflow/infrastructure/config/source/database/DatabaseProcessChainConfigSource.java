package com.zornflow.infrastructure.config.source.database;

import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.model.ProcessNodeConfig;
import com.zornflow.infrastructure.config.model.RecordStatus;
import com.zornflow.infrastructure.persistence.jooq.tables.records.ChainNodesRecord;
import com.zornflow.infrastructure.persistence.jooq.tables.records.ProcessChainsRecord;
import com.zornflow.infrastructure.persistence.jooq.tables.records.SharedNodesRecord;
import com.zornflow.infrastructure.persistence.mapper.ProcessPersistenceMapper;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.zornflow.infrastructure.persistence.jooq.Tables.*;

@Component
public non-sealed class DatabaseProcessChainConfigSource extends AbstractDatabaseConfigSource<ProcessChainConfig> {

  private final ProcessPersistenceMapper processPersistenceMapper;

  public DatabaseProcessChainConfigSource(
    DSLContext dsl,
    ProcessPersistenceMapper processPersistenceMapper
  ) {
    super(dsl);
    this.processPersistenceMapper = processPersistenceMapper;
  }

  @Override
  protected Optional<ProcessChainConfig> loadById(String id) {
    ProcessChainsRecord chainRecord = dsl.selectFrom(PROCESS_CHAINS)
      .where(PROCESS_CHAINS.ID.eq(id).and(PROCESS_CHAINS.RECORD_STATUS.eq(RecordStatus.ACTIVE.getDbValue())))
      .fetchOne();

    if (chainRecord == null) {
      return Optional.empty();
    }

    List<ProcessNodeConfig> nodes = findNodesForChain(id);
    return Optional.of(processPersistenceMapper.toDto(chainRecord, nodes));
  }

  @Override
  public Map<String, ProcessChainConfig> loadAll() {
    List<ProcessChainsRecord> chainRecords = dsl.selectFrom(PROCESS_CHAINS)
      .where(PROCESS_CHAINS.RECORD_STATUS.eq(RecordStatus.ACTIVE.getDbValue()))
      .fetch();
    return chainRecords.stream()
      .map(r -> processPersistenceMapper.toDto(r, findNodesForChain(r.getId())))
      .collect(Collectors.toMap(ProcessChainConfig::id, config -> config));
  }

  private List<ProcessNodeConfig> findNodesForChain(String chainId) {
    return dsl.select()
      .from(CHAIN_NODES)
      .leftJoin(SHARED_NODES).on(CHAIN_NODES.SHARED_NODE_ID.eq(SHARED_NODES.ID))
      .where(CHAIN_NODES.PROCESS_CHAIN_ID.eq(chainId))
      .orderBy(CHAIN_NODES.SEQUENCE.asc())
      .fetch()
      .stream()
      .map(record -> {
        ChainNodesRecord instanceRecord = record.into(CHAIN_NODES);
        SharedNodesRecord templateRecord = record.into(SHARED_NODES);

        // Step 1: Map the instance record to its DTO.
        ProcessNodeConfig instanceConfig = processPersistenceMapper.toDto(instanceRecord);

        // Step 2: If a template exists, map it and merge.
        if (templateRecord.getId() != null) {
          ProcessNodeConfig templateConfig = processPersistenceMapper.toDto(templateRecord);
          return mergeNodeConfigs(instanceConfig, templateConfig);
        } else {
          return instanceConfig;
        }
      })
      .collect(Collectors.toList());
  }

  private ProcessNodeConfig mergeNodeConfigs(ProcessNodeConfig instance, ProcessNodeConfig template) {
    return ProcessNodeConfig.builder()
      .id(instance.id()) // Always from instance
      .sharedNodeId(Optional.of(template.id())) // Link to template
      .name(Optional.ofNullable(instance.name()).orElse(template.name()))
      .next(instance.next()) // `next` only exists on the instance
      .type(Optional.ofNullable(instance.type()).orElse(template.type()))
      .ruleChain(Optional.ofNullable(instance.ruleChain()).orElse(template.ruleChain()))
      .conditions(Optional.ofNullable(instance.conditions()).filter(c -> !c.isEmpty()).orElse(template.conditions()))
      .properties(Optional.ofNullable(instance.properties()).filter(p -> !p.isEmpty()).orElse(template.properties()))
      .status(template.status()) // Status from template
      .version(instance.version())
      .createdAt(instance.createdAt())
      .updatedAt(instance.updatedAt())
      .build();
  }


  @Override
  @Transactional
  public Optional<ProcessChainConfig> save(ProcessChainConfig modelConfig) {
    validateChain(modelConfig);

    String chainId = modelConfig.id();
    OffsetDateTime now = OffsetDateTime.now();

    ProcessChainsRecord chainRecord = dsl.fetchOne(PROCESS_CHAINS, PROCESS_CHAINS.ID.eq(chainId));
    if (chainRecord == null) {
      chainRecord = dsl.newRecord(PROCESS_CHAINS);
      chainRecord.setCreatedAt(now);
    }
    processPersistenceMapper.updateRecord(modelConfig, chainRecord);
    chainRecord.setUpdatedAt(now);

    chainRecord.store();

    dsl.deleteFrom(CHAIN_NODES).where(CHAIN_NODES.PROCESS_CHAIN_ID.eq(chainId)).execute();

    if (modelConfig.nodes() != null && !modelConfig.nodes().isEmpty()) {
      AtomicInteger sequence = new AtomicInteger(0);
      List<ChainNodesRecord> nodeRecords = modelConfig.nodes().stream()
        .map(dto -> {
          ChainNodesRecord record = processPersistenceMapper.toRecord(dto, chainId, sequence.getAndIncrement());
          record.setCreatedAt(now);
          record.setUpdatedAt(now);
          return record;
        })
        .collect(Collectors.toList());
      dsl.batchInsert(nodeRecords).execute();
    }
    return this.loadById(modelConfig.id());
  }

  @Override
  @Transactional
  public void delete(String id) {
    dsl.update(PROCESS_CHAINS)
      .set(PROCESS_CHAINS.RECORD_STATUS, RecordStatus.DELETED.getDbValue())
      .set(PROCESS_CHAINS.UPDATED_AT, OffsetDateTime.now())
      .where(PROCESS_CHAINS.ID.eq(id))
      .execute();
  }

  private void validateChain(ProcessChainConfig config) {
    Objects.requireNonNull(config, "ProcessChainConfig cannot be null.");
    Objects.requireNonNull(config.id(), "ProcessChain ID cannot be null.");
    Objects.requireNonNull(config.name(), "ProcessChain name cannot be null.");

    if (config.nodes() != null && !config.nodes().isEmpty()) {
      Map<String, ProcessNodeConfig> nodeMap = config.nodes().stream()
        .collect(Collectors.toMap(ProcessNodeConfig::id, node -> node));

      for (ProcessNodeConfig node : config.nodes()) {
        if (node.sharedNodeId().isEmpty()) {
          if (node.name() == null || node.name().isBlank()) {
            throw new IllegalArgumentException("Inline node (id=" + node.id() + ") must have a name.");
          }
          if (node.type() == null) {
            throw new IllegalArgumentException("Inline node (id=" + node.id() + ") must have a type.");
          }
        }
        if (node.next() != null && !node.next().isEmpty() && !nodeMap.containsKey(node.next())) {
          throw new IllegalArgumentException("Node (id=" + node.id() + ") points to a non-existent next node (id=" + node.next() + ") within the same chain.");
        }
      }

      Set<String> visited = new HashSet<>();
      Set<String> recursionStack = new HashSet<>();
      for (ProcessNodeConfig node : config.nodes()) {
        if (!visited.contains(node.id())) {
          if (hasCycle(node.id(), nodeMap, visited, recursionStack)) {
            throw new IllegalStateException("A cycle was detected in the process chain, starting from node: " + node.id());
          }
        }
      }
    }
  }

  private boolean hasCycle(String nodeId, Map<String, ProcessNodeConfig> nodeMap, Set<String> visited, Set<String> recursionStack) {
    visited.add(nodeId);
    recursionStack.add(nodeId);

    ProcessNodeConfig currentNode = nodeMap.get(nodeId);
    if (currentNode != null && currentNode.next() != null && !currentNode.next().isEmpty()) {
      String nextNodeId = currentNode.next();
      if (nodeMap.containsKey(nextNodeId)) {
        if (!visited.contains(nextNodeId)) {
          if (hasCycle(nextNodeId, nodeMap, visited, recursionStack)) {
            return true;
          }
        } else if (recursionStack.contains(nextNodeId)) {
          return true;
        }
      }
    }

    recursionStack.remove(nodeId);
    return false;
  }
}
