package com.zornflow.gateway.domain.spi;

import reactor.core.publisher.Mono;

/**
 * 加密引擎SPI接口 (策略接口).
 * 负责执行具体地加解密算法。实现类应处理所有与算法相关的细节，如IV的生成和拼接。
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 14:13
 **/

public interface CryptoEngine {

  /**
   * 加密操作
   * 使用 Mono.fromCallable(...) 或 Mono.fromRunnable(...) 将同步的、可能阻塞的代码块封装成一个 Mono
   * 再使用 .subscribeOn(...) 为这个 Mono 指定一个专门的线程池来执行
   * 对于IO密集型阻塞（如JDBC数据库查询、文件读写、调用阻塞的SDK）blockingMono.subscribeOn(Schedulers.boundedElastic())
   * 对于CPU密集型阻塞（如复杂的计算、加解密）blockingMono.subscribeOn(Schedulers.parallel())
   *
   * @param plainText 明文数据
   * @param key       密钥
   * @return 加密后的数据（通常包含IV等信息）
   */
  Mono<byte[]> encrypt(byte[] plainText, byte[] key);

  /**
   * 解密操作
   * 使用 Mono.fromCallable(...) 或 Mono.fromRunnable(...) 将同步的、可能阻塞的代码块封装成一个 Mono
   * 再使用 .subscribeOn(...) 为这个 Mono 指定一个专门的线程池来执行
   * 对于IO密集型阻塞（如JDBC数据库查询、文件读写、调用阻塞的SDK）blockingMono.subscribeOn(Schedulers.boundedElastic())
   * 对于CPU密集型阻塞（如复杂的计算、加解密）blockingMono.subscribeOn(Schedulers.parallel())
   *
   * @param encryptedText 密文数据（通常包含IV等信息）
   * @param key        密钥
   * @return 解密后的明文数据
   */
  Mono<byte[]> decrypt(byte[] encryptedText, byte[] key);
}
