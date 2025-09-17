package com.zornflow.gateway.infrastructure.crypto.keysource.configfile;

import com.zornflow.gateway.domain.spi.SecretKeySource;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基于配置文件的密钥源实现，支持多版本密钥和密钥轮换。
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.1
 * @since 2025/9/14 13:53
 **/
@Component
@ConditionalOnProperty(name = "security.crypto.key-source-strategy", havingValue = "CONFIG_FILE")
public class ConfigFileKeySource implements SecretKeySource {

  private final ConfigFileCryptoConfig config;
  private Map<String, String> keyMap;
  private ConfigFileCryptoConfig.KeyEntry primaryKey;

  public ConfigFileKeySource(ConfigFileCryptoConfig config) {
    this.config = config;
  }

  @PostConstruct
  public void init() {
    // 在服务启动时加载并解析密钥配置
    this.keyMap = config.keys().stream()
      .collect(Collectors.toMap(ConfigFileCryptoConfig.KeyEntry::version, ConfigFileCryptoConfig.KeyEntry::secret));

    this.primaryKey = config.keys().stream()
      .filter(ConfigFileCryptoConfig.KeyEntry::primary)
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("No primary key configured. Please mark one key with 'primary: true'."));

    if (config.keys().stream().filter(ConfigFileCryptoConfig.KeyEntry::primary).count() > 1) {
      throw new IllegalStateException("Multiple primary keys configured. Only one key can be marked as 'primary: true'.");
    }
  }

  @Override
  public Mono<String> obtainKey(String keyId, String version) {
    // 如果提供了版本号，则按版本查找
    if (version != null && !version.isBlank()) {
      return Mono.justOrEmpty(keyMap.get(version))
        .switchIfEmpty(Mono.error(new SecurityException("Key for version '" + version + "' not found.")));
    }
    // 如果未提供版本号，默认返回主密钥（用于解密时的降级尝试）
    return obtainPrimaryKey().map(SecretKeySource.KeyDetail::secret);
  }

  @Override
  public Mono<KeyDetail> obtainPrimaryKey() {
    // 直接返回内存中已确定的主密钥
    return Mono.just(new KeyDetail(primaryKey.version(), primaryKey.secret()));
  }
}
