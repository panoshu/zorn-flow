package com.zornflow.gateway.infrastructure.crypto.keysource.vault;

import com.zornflow.gateway.domain.spi.SecretKeySource;
import com.zornflow.gateway.infrastructure.properties.CryptoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.core.VaultTransitOperations;
import org.springframework.vault.support.TransitKeyType;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "security.crypto.key-source-strategy", havingValue = "VAULT")
public class VaultKeySource implements SecretKeySource {

  private final VaultTemplate vaultTemplate;
  private final CryptoProperties cryptoProperties;

  @Override
  public Mono<String> obtainKey(String keyId, String version) {
    return Mono.defer(() -> {
        Map<String, String> keys = Objects.requireNonNull(
            vaultTemplate.opsForTransit().exportKey(keyId, TransitKeyType.ENCRYPTION_KEY)
          ).getKeys();
        return Mono.justOrEmpty(keys.get(version));
      })
      .switchIfEmpty(Mono.error(new IllegalArgumentException("Key version not found: " + version)))
      .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<KeyDetail> obtainPrimaryKey() {
    return Mono.defer(() -> {
        VaultTransitOperations ops = vaultTemplate.opsForTransit();
        String keyId = cryptoProperties.keyId();
        int latestVer = Objects.requireNonNull(ops.getKey(keyId)).getLatestVersion();

        String keyMat = Objects.requireNonNull(ops.exportKey(keyId, TransitKeyType.ENCRYPTION_KEY))
          .getKeys()
          .get(String.valueOf(latestVer));
        return Mono.justOrEmpty(keyMat)
          .map(k -> new KeyDetail(String.valueOf(latestVer), k));
      })
      .switchIfEmpty(Mono.error(new IllegalStateException("Primary key material is empty")))
      .subscribeOn(Schedulers.boundedElastic());
  }
}
