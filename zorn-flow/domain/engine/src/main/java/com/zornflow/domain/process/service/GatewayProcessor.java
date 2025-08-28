package com.zornflow.domain.process.service;

import com.zornflow.domain.common.valueobject.BusinessContext;
import com.zornflow.domain.process.entity.ProcessNode;
import com.zornflow.domain.process.types.ProcessNodeId;

/**
 * 网关处理器接口
 * 负责处理网关节点的逻辑，根据条件选择下一个节点
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/25 23:00
 **/

public interface GatewayProcessor {
  ProcessNodeId process(ProcessNode gatewayNode, BusinessContext context);
}
