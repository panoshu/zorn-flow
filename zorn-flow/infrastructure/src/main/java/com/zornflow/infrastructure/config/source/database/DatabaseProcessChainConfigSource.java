package com.zornflow.infrastructure.config.source.database;

import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.model.ProcessNodeConfig;
import com.zornflow.infrastructure.config.model.RecordStatus;
import com.zornflow.infrastructure.persistence.jooq.tables.records.ChainNodesRecord;
import com.zornflow.infrastructure.persistence.jooq.tables.records.ProcessChainsRecord;
import com.zornflow.infrastructure.persistence.jooq.tables.records.SharedNodesRecord;
import com.zornflow.infrastructure.persistence.mapper.JsonbMapperHelper;
import com.zornflow.infrastructure.persistence.mapper.ProcessPersistenceMapper;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.zornflow.infrastructure.persistence.jooq.Tables.*;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/1 15:21
 **/

@Component
public non-sealed class DatabaseProcessChainConfigSource extends AbstractDatabaseConfigSource<ProcessChainConfig> {

  private final ProcessPersistenceMapper processPersistenceMapper;
  private final JsonbMapperHelper jsonbMapperHelper;

  public DatabaseProcessChainConfigSource(
    DSLContext dsl,
    ProcessPersistenceMapper processPersistenceMapper,
    JsonbMapperHelper jsonbMapperHelper
  ) {
    super(dsl);
    this.processPersistenceMapper = processPersistenceMapper;
    this.jsonbMapperHelper = jsonbMapperHelper;
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
        ChainNodesRecord instance = record.into(CHAIN_NODES);
        SharedNodesRecord template = record.into(SHARED_NODES);
        if (template.getId() != null) {
          return processPersistenceMapper.toDto(template, instance, jsonbMapperHelper);
        } else {
          return processPersistenceMapper.toDto(instance, jsonbMapperHelper);
        }
      })
      .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public Optional<ProcessChainConfig> save(ProcessChainConfig modelConfig) {
    validateChain(modelConfig);

    String chainId = modelConfig.id();
    OffsetDateTime now = OffsetDateTime.now();

    // 1. Map DTO to a new or existing record
    ProcessChainsRecord chainRecord = dsl.fetchOne(PROCESS_CHAINS, PROCESS_CHAINS.ID.eq(chainId));
    if (chainRecord == null) {
      chainRecord = dsl.newRecord(PROCESS_CHAINS);
      // Set creation time only for new records
      chainRecord.setCreatedAt(now);
    }
    processPersistenceMapper.updateRecord(modelConfig, chainRecord);
    // Always set update time
    chainRecord.setUpdatedAt(now);

    // 2. Use JOOQ's store() method for a clean insert or update
    chainRecord.store();

    // 3. Handle child records (nodes)
    dsl.deleteFrom(CHAIN_NODES).where(CHAIN_NODES.PROCESS_CHAIN_ID.eq(chainId)).execute();

    if (modelConfig.nodes() != null && !modelConfig.nodes().isEmpty()) {
      AtomicInteger sequence = new AtomicInteger(0);
      List<ChainNodesRecord> nodeRecords = modelConfig.nodes().stream()
        .map(dto -> {
          ChainNodesRecord record = processPersistenceMapper.toRecord(dto, chainId, sequence.getAndIncrement(), jsonbMapperHelper);
          record.setCreatedAt(now); // Also set timestamps for child records
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
        if (node.next() != null && !nodeMap.containsKey(node.next())) {
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
    if (currentNode != null && currentNode.next() != null) {
      String nextNodeId = currentNode.next();
      if (nodeMap.containsKey(nextNodeId)) { // Ensure next node exists before proceeding
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
