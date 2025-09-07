package com.zornflow.infrastructure.config.source.database;

import com.zornflow.infrastructure.config.model.RecordStatus;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.model.RuleConfig;
import com.zornflow.infrastructure.persistence.jooq.tables.records.ChainRulesRecord;
import com.zornflow.infrastructure.persistence.jooq.tables.records.RuleChainsRecord;
import com.zornflow.infrastructure.persistence.jooq.tables.records.SharedRulesRecord;
import com.zornflow.infrastructure.persistence.mapper.JsonbMapperHelper;
import com.zornflow.infrastructure.persistence.mapper.RulePersistenceMapper;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.zornflow.infrastructure.persistence.jooq.Tables.*;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/1 14:54
 **/

@Component
public non-sealed class DatabaseRuleChainConfigSource extends AbstractDatabaseConfigSource<RuleChainConfig> {

  private final RulePersistenceMapper rulePersistenceMapper;
  private final JsonbMapperHelper jsonbMapperHelper;

  public DatabaseRuleChainConfigSource(
    DSLContext dsl,
    RulePersistenceMapper rulePersistenceMapper,
    JsonbMapperHelper jsonbMapperHelper
  ) {
    super(dsl);
    this.rulePersistenceMapper = rulePersistenceMapper;
    this.jsonbMapperHelper = jsonbMapperHelper;
  }

  @Override
  protected Optional<RuleChainConfig> loadById(String id) {
    RuleChainsRecord chainRecord = dsl.selectFrom(RULE_CHAINS)
      .where(RULE_CHAINS.ID.eq(id)
            .and(RULE_CHAINS.RECORD_STATUS.eq(RecordStatus.ACTIVE.getDbValue())))
      .fetchOne();

    if (chainRecord == null) {
      return Optional.empty();
    }

    List<RuleConfig> rules = findRulesForChain(id);
    return Optional.of(rulePersistenceMapper.toDto(chainRecord, rules));
  }

  @Override
  public Map<String, RuleChainConfig> loadAll() {
    List<RuleChainsRecord> chainRecords = dsl.selectFrom(RULE_CHAINS)
      .where(RULE_CHAINS.RECORD_STATUS.eq(RecordStatus.ACTIVE.getDbValue()))
      .fetch();
    return chainRecords.stream()
      .map(r -> rulePersistenceMapper.toDto(r, findRulesForChain(r.getId())))
      .collect(Collectors.toMap(RuleChainConfig::id, config -> config));
  }

  private List<RuleConfig> findRulesForChain(String chainId) {
    return dsl.select()
      .from(CHAIN_RULES)
      .leftJoin(SHARED_RULES).on(CHAIN_RULES.SHARED_RULE_ID.eq(SHARED_RULES.ID))
      .where(CHAIN_RULES.RULE_CHAIN_ID.eq(chainId))
      .orderBy(CHAIN_RULES.SEQUENCE.asc())
      .fetch()
      .stream()
      .map(record -> {
        ChainRulesRecord instance = record.into(CHAIN_RULES);
        SharedRulesRecord template = record.into(SHARED_RULES);
        if (template.getId() != null) {
          return rulePersistenceMapper.toDto(template, instance, jsonbMapperHelper);
        } else {
          return rulePersistenceMapper.toDto(instance, jsonbMapperHelper);
        }
      })
      .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public Optional<RuleChainConfig> save(RuleChainConfig modelConfig) {
    validateChain(modelConfig);

    String chainId = modelConfig.id();
    OffsetDateTime now = OffsetDateTime.now();

    // 1. Map DTO to a new or existing record
    RuleChainsRecord chainRecord = dsl.fetchOne(RULE_CHAINS, RULE_CHAINS.ID.eq(chainId));
    if (chainRecord == null) {
      chainRecord = dsl.newRecord(RULE_CHAINS);
      // Set creation time only for new records
      chainRecord.setCreatedAt(now);
    }
    rulePersistenceMapper.updateRecord(modelConfig, chainRecord);
    // Always set update time
    chainRecord.setUpdatedAt(now);

    // 2. Use JOOQ's store() method for a clean insert or update
    chainRecord.store();

    // 3. Handle child records (rules)
    dsl.deleteFrom(CHAIN_RULES).where(CHAIN_RULES.RULE_CHAIN_ID.eq(chainId)).execute();

    if (modelConfig.rules() != null && !modelConfig.rules().isEmpty()) {
      AtomicInteger sequence = new AtomicInteger(0);
      List<ChainRulesRecord> ruleRecords = modelConfig.rules().stream()
        .map(dto -> {
          ChainRulesRecord record = rulePersistenceMapper.toRecord(dto, chainId, sequence.getAndIncrement(), jsonbMapperHelper);
          record.setCreatedAt(now); // Also set timestamps for child records
          record.setUpdatedAt(now);
          return record;
        })
        .collect(Collectors.toList());
      dsl.batchInsert(ruleRecords).execute();
    }

    return this.loadById(modelConfig.id());
  }

  @Override
  @Transactional
  public void delete(String id) {
    dsl.update(RULE_CHAINS)
      .set(RULE_CHAINS.RECORD_STATUS, RecordStatus.DELETED.getDbValue())
      .set(RULE_CHAINS.UPDATED_AT, OffsetDateTime.now())
      .where(RULE_CHAINS.ID.eq(id))
      .execute();
  }

  private void validateChain(RuleChainConfig config) {
    Objects.requireNonNull(config, "RuleChainConfig cannot be null.");
    Objects.requireNonNull(config.id(), "RuleChain ID cannot be null.");
    Objects.requireNonNull(config.name(), "RuleChain name cannot be null.");

    if (config.rules() != null) {
      for (RuleConfig rule : config.rules()) {
        if (rule.sharedRuleId().isEmpty()) {
          if (rule.name() == null || rule.name().isBlank()) {
            throw new IllegalArgumentException("Inline rule (id=" + rule.id() + ") must have a name.");
          }
          if (rule.priority() == null) {
            throw new IllegalArgumentException("Inline rule (id=" + rule.id() + ") must have a priority.");
          }
          if (rule.handle() == null) {
            throw new IllegalArgumentException("Inline rule (id=" + rule.id() + ") must have a handlerConfig.");
          }
        }
      }
    }
  }
}
