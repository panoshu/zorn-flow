package com.zornflow.domain.process.repository;

import com.ddd.contract.repository.BaseRepository;
import com.zornflow.domain.process.entity.ProcessInstance;
import com.zornflow.domain.process.types.ProcessInstanceId;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/25 22:54
 **/

public interface ProcessInstanceRepository extends BaseRepository<ProcessInstance, ProcessInstanceId> {
}
