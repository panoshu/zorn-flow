package com.zornflow.infrastructure.config.source.cache;

import com.zornflow.domain.common.config.source.ReadWriteConfigSource;
import com.zornflow.infrastructure.config.model.RecordStatus;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.model.RuleConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractCachingCompositeConfigSourceDecoratorTest {

  private static final String CACHE_NAME = "ruleChains";
  private static final String ALL_CACHE_KEY = "__ALL_" + CACHE_NAME.toUpperCase() + "__";
  private static final String ITEM_KEY_PREFIX = CACHE_NAME + "::";

  private AbstractCachingCompositeConfigSourceDecorator<RuleChainConfig> cachingDecorator;

  @Mock
  private ReadWriteConfigSource<RuleChainConfig> delegate;

  private Cache cache;

  private RuleChainConfig sampleConfig1;
  private RuleChainConfig sampleConfig2;

  @BeforeEach
  void setUp() {
    CacheManager cacheManager = new ConcurrentMapCacheManager(CACHE_NAME);
    cache = cacheManager.getCache(CACHE_NAME);
    assertNotNull(cache);
    cache.clear();

    cachingDecorator = new CachingRuleChainCompositeConfigSourceDecorator(delegate, cacheManager);

    sampleConfig1 = new RuleChainConfig("id-1", "Chain 1", "Desc 1",
      Collections.singletonList(RuleConfig.builder().id("rule-1").build()),
      RecordStatus.ACTIVE.getDbValue(), 0, null, null);
    sampleConfig2 = new RuleChainConfig("id-2", "Chain 2", "Desc 2",
      Collections.singletonList(RuleConfig.builder().id("rule-2").build()),
      RecordStatus.ACTIVE.getDbValue(), 0, null, null);
  }

  @Test
  @DisplayName("loan(id): Should fetch from delegate on cache miss and populate cache")
  void load_shouldFetchFromDelegateOnCacheMiss() throws IOException {
    // Arrange
    when(delegate.load("id-1")).thenReturn(Optional.of(sampleConfig1));

    // Act: First loan, should be a cache miss
    Optional<RuleChainConfig> result1 = cachingDecorator.load("id-1");

    // Assert
    verify(delegate, times(1)).load("id-1");
    assertTrue(result1.isPresent());
    assertEquals("Chain 1", result1.get().name());
    assertNotNull(cache.get(ITEM_KEY_PREFIX + "id-1"));

    // Act: Second loan, should be a cache hit
    Optional<RuleChainConfig> result2 = cachingDecorator.load("id-1");

    // Assert
    verify(delegate, times(1)).load("id-1");
    assertTrue(result2.isPresent());
  }

  @Test
  @DisplayName("loadAll(): Should fetch all from delegate on cache miss and populate caches")
  void loadAll_shouldFetchFromDelegateOnCacheMiss() throws IOException {
    // Arrange
    when(delegate.loadAll()).thenReturn(Map.of("id-1", sampleConfig1, "id-2", sampleConfig2));

    // Act: First loadAll, should be a miss
    Map<String, RuleChainConfig> allConfigs = cachingDecorator.loadAll();

    // Assert
    verify(delegate, times(1)).loadAll();
    assertEquals(2, allConfigs.size());
    assertNotNull(cache.get(ALL_CACHE_KEY));
    assertNotNull(cache.get(ITEM_KEY_PREFIX + "id-1"));
    assertNotNull(cache.get(ITEM_KEY_PREFIX + "id-2"));

    // Act: Second loadAll, should be a hit
    cachingDecorator.loadAll();

    // Assert
    verify(delegate, times(1)).loadAll();
  }

  @Test
  @DisplayName("save(): Should call delegate and evict caches")
  void save_shouldCallDelegateAndEvictCaches() throws IOException {
    // Arrange
    when(delegate.save(any(RuleChainConfig.class))).thenAnswer(invocation -> Optional.of(invocation.getArgument(0)));
    cachingDecorator.load("id-1");
    cachingDecorator.loadAll();
    RuleChainConfig updatedConfig = new RuleChainConfig("id-1", "Updated Chain", "New Desc",
      List.of(), RecordStatus.ACTIVE.getDbValue(), 0, null, null);

    // Act
    cachingDecorator.save(updatedConfig);

    // Assert
    verify(delegate, times(1)).save(updatedConfig);
    assertEquals("Updated Chain", ((RuleChainConfig) cache.get(ITEM_KEY_PREFIX + "id-1").get()).name());
    assertNull(cache.get(ALL_CACHE_KEY));
  }

  @Test
  @DisplayName("delete(): Should call delegate and evict caches")
  void delete_shouldCallDelegateAndEvictCaches() {
    // Arrange
    cachingDecorator.loadAll(); // Pre-populate cache

    // Act
    cachingDecorator.delete("id-1");

    // Assert
    verify(delegate, times(1)).delete("id-1");
    assertNull(cache.get(ITEM_KEY_PREFIX + "id-1"));
    assertNull(cache.get(ALL_CACHE_KEY));
  }

  @Test
  @DisplayName("refresh(): Should clear cache and reload all data from delegate")
  void refresh_shouldClearAndReloadCache() throws IOException {
    // Arrange
    when(delegate.loadAll()).thenReturn(Map.of("id-1", sampleConfig1, "id-2", sampleConfig2));
    cache.put(ITEM_KEY_PREFIX + "id-1", "stale_data");

    // Act
    cachingDecorator.refresh();

    // Assert
    verify(delegate, times(1)).loadAll();
    assertNotNull(cache.get(ALL_CACHE_KEY));
    RuleChainConfig cachedItem = (RuleChainConfig) cache.get(ITEM_KEY_PREFIX + "id-1").get();
    assertEquals("Chain 1", cachedItem.name());
  }
}
