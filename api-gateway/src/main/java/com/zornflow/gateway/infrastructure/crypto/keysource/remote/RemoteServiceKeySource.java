package com.zornflow.gateway.infrastructure.crypto.keysource.remote;

import com.zornflow.gateway.domain.spi.SecretKeySource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 13:58
 **/

@Component
@ConditionalOnProperty(name = "security.crypto.key-source-strategy", havingValue = "REMOTE_SERVICE")
@RequiredArgsConstructor
public class RemoteServiceKeySource implements SecretKeySource {
  private final RemoteServiceConfig config;
  private final WebClient client = WebClient.create();

  /**
   * 根据 keyId 和 version 从远程服务获取密钥。
   * 对应 KeyController 中带 @RequestParam 的 GET 方法。
   * @param keyId 密钥的唯一标识符
   * @param version 密钥的版本标识。如果为 null, 则获取主密钥。
   * @return Base64编码的密钥字符串
   */
  @Override
  public Mono<String> obtainKey(String keyId, String version) {
    // 使用 UriComponentsBuilder 来动态构建 URL
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(config.url());

    // 如果 version 不为 null 或空，则将其作为查询参数附加
    if (version != null && !version.isBlank()) {
      builder.queryParam("version", version);
    }

    // URI 模板变量 {keyId} 会被替换
    String finalUrl = builder.buildAndExpand(Map.of("keyId", keyId)).toUriString();

    return client.get()
      .uri(finalUrl)
      .retrieve()
      .bodyToMono(KeyDetailResponse.class) // 期望远程服务返回一个只包含密钥的简单 JSON 对象
      .map(KeyDetailResponse::secretKey);
  }

  /**
   * 从远程服务获取当前活动的主密钥。
   * 远程服务应设计为：当不带 version 参数请求时，默认返回主密钥。
   * @return 包含主密钥版本和内容的 KeyDetail 对象
   */
  @Override
  public Mono<KeyDetail> obtainPrimaryKey() {
    // 不传递 version 参数来请求主密钥
    String finalUrl = UriComponentsBuilder.fromUriString(config.url())
      .buildAndExpand(Map.of("keyId", "default-key"))
      .toUriString();

    return client.get()
      .uri(finalUrl)
      .retrieve()
      .bodyToMono(KeyDetail.class); // 期望远程服务直接返回 KeyDetail 结构
  }

  /**
   * 用于解析远程服务响应体的内部 record。
   * 假设远程服务返回的 JSON 是 {"secretKey": "..."}
   */
  private record KeyDetailResponse(String secretKey) {}

}
