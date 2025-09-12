package com.zornflow.infrastructure.config.source.database;

import com.zornflow.infrastructure.config.model.RecordStatus;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.model.RuleConfig;
import com.zornflow.infrastructure.persistence.jooq.tables.records.ChainRulesRecord;
import com.zornflow.infrastructure.persistence.jooq.tables.records.RuleChainsRecord;
import com.zornflow.infrastructure.persistence.jooq.tables.records.SharedRulesRecord;
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

@Component
public non-sealed class DatabaseRuleChainConfigSource extends AbstractDatabaseConfigSource<RuleChainConfig> {

  private final RulePersistenceMapper rulePersistenceMapper;

  public DatabaseRuleChainConfigSource(
    DSLContext dsl,
    RulePersistenceMapper rulePersistenceMapper
  ) {
    super(dsl);
    this.rulePersistenceMapper = rulePersistenceMapper;
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
        ChainRulesRecord instanceRecord = record.into(CHAIN_RULES);
        SharedRulesRecord templateRecord = record.into(SHARED_RULES);

        RuleConfig instanceConfig = rulePersistenceMapper.toDto(instanceRecord);

        if (templateRecord.getId() != null) {
          RuleConfig templateConfig = rulePersistenceMapper.toDto(templateRecord);
          return mergeRuleConfigs(instanceConfig, templateConfig);
        } else {
          return instanceConfig;
        }
      })
      .collect(Collectors.toList());
  }

  private RuleConfig mergeRuleConfigs(RuleConfig instance, RuleConfig template) {
    return RuleConfig.builder()
      .id(instance.id()) // Always from instance
      .sharedRuleId(Optional.of(template.id())) // Link to template
      .name(Optional.ofNullable(instance.name()).orElse(template.name()))
      .priority(Optional.ofNullable(instance.priority()).orElse(template.priority()))
      .condition(Optional.ofNullable(instance.condition()).orElse(template.condition()))
      .handle(Optional.ofNullable(instance.handle()).orElse(template.handle()))
      .status(template.status()) // Status from template
      .version(instance.version())
      .createdAt(instance.createdAt())
      .updatedAt(instance.updatedAt())
      .build();
  }

  @Override
  @Transactional
  public Optional<RuleChainConfig> save(RuleChainConfig modelConfig) {
    validateChain(modelConfig);

    String chainId = modelConfig.id();
    OffsetDateTime now = OffsetDateTime.now();

    RuleChainsRecord chainRecord = dsl.fetchOne(RULE_CHAINS, RULE_CHAINS.ID.eq(chainId));
    if (chainRecord == null) {
      chainRecord = dsl.newRecord(RULE_CHAINS);
      chainRecord.setCreatedAt(now);
    }
    rulePersistenceMapper.updateRecord(modelConfig, chainRecord);
    chainRecord.setUpdatedAt(now);

    chainRecord.store();

    dsl.deleteFrom(CHAIN_RULES).where(CHAIN_RULES.RULE_CHAIN_ID.eq(chainId)).execute();

    if (modelConfig.rules() != null && !modelConfig.rules().isEmpty()) {
      AtomicInteger sequence = new AtomicInteger(0);
      List<ChainRulesRecord> ruleRecords = modelConfig.rules().stream()
        .map(dto -> {
          ChainRulesRecord record = rulePersistenceMapper.toRecord(dto, chainId, sequence.getAndIncrement());
          record.setCreatedAt(now);
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
