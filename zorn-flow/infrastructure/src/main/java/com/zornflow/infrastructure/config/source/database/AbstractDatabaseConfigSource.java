package com.zornflow.infrastructure.config.source.database;

import com.zornflow.domain.common.config.model.ModelConfig;
import com.zornflow.domain.common.config.source.ReadWriteConfigSource;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;

import java.io.IOException;
import java.util.Optional;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/31 20:27
 **/

@RequiredArgsConstructor
public sealed abstract class AbstractDatabaseConfigSource<T extends ModelConfig> implements ReadWriteConfigSource<T>
permits DatabaseRuleChainConfigSource, DatabaseProcessChainConfigSource{

  protected final DSLContext dsl;

  @Override
  public Optional<T> load(String id) throws IOException {
    return this.loadById(id);
  }

  // 将 loan(String) 委托给一个更具体的名字，避免重写警告
  protected abstract Optional<T> loadById(String id);

  @Override
  public ConfigSourceType getSourceType() {
    return ConfigSourceType.DATABASE;
  }

  /**
   * Checks if the database source is available by executing a simple query.
   * @return true if the database is reachable, false otherwise.
   */
  @Override
  public boolean available() {
    try {
      // A simple, fast query to verify the database connection is alive.
      dsl.selectOne().fetch();
      return true;
    } catch (DataAccessException e) {
      // This exception typically wraps connection problems.
      // It's good practice to log this in a real application.
      return false;
    }
  }
}
