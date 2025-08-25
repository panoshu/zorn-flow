package com.zornflow.domain.rule.repository;

import com.ddd.contract.repository.BaseRepository;
import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.domain.rule.types.RuleChainId;

/**
 * 规则链配置仓库接口
 * 定义配置的加载、保存和刷新操作
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/1 11:05
 */

public interface RuleChainRepository extends BaseRepository<RuleChain, RuleChainId> {
}
