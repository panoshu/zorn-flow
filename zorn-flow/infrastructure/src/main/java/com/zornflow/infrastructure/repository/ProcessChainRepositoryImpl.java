package com.zornflow.infrastructure.repository;

import com.zornflow.domain.process.entity.ProcessChain;
import com.zornflow.domain.process.repository.ProcessChainRepository;
import com.zornflow.domain.process.types.ProcessChainId;
import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.source.cache.CachingProcessChainCompositeConfigSourceDecorator;
import com.zornflow.infrastructure.mapper.ProcessDomainMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/1 17:33
 **/

@Repository
@RequiredArgsConstructor
public class ProcessChainRepositoryImpl implements ProcessChainRepository {

  private final CachingProcessChainCompositeConfigSourceDecorator configSource;
  private final ProcessDomainMapper mapper;

  @Override
  @SneakyThrows
  public Optional<ProcessChain> findById(ProcessChainId processChainId) {
    return configSource.load(processChainId.value())
      .map(mapper::toDomain);
  }

  @Override
  @SneakyThrows
  public List<ProcessChain> findAll() {
    return configSource.loadAll().values().stream()
      .map(mapper::toDomain)
      .collect(Collectors.toList());
  }

  @Override
  @SneakyThrows
  public ProcessChain save(ProcessChain processChain) {
    ProcessChainConfig dtoToSave = mapper.toDto(processChain);
    Optional<ProcessChainConfig> savedDtoOptional = configSource.save(dtoToSave);
    return savedDtoOptional
      .map(mapper::toDomain)
      .orElseThrow(() -> new IllegalStateException("Config source failed to return the saved entity for ID: " + processChain.getId().value()));
  }

  @Override
  @SneakyThrows
  public void delete(ProcessChain processChain) {
    this.deleteById(processChain.getId());
  }

  @Override
  public void deleteById(ProcessChainId processChainId) {
    configSource.delete(processChainId.value());
  }
}
