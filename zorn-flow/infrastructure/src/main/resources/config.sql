-- 全局规则模板
CREATE TABLE cfg_rule (
                        id          VARCHAR(64)  PRIMARY KEY,
                        name        VARCHAR(128) NOT NULL,
                        priority    INT          NOT NULL DEFAULT 100,
                        condition_  TEXT         NOT NULL,
                        handle_type VARCHAR(16)  NOT NULL,
                        handler     VARCHAR(256) NOT NULL,
                        parameters  JSON,
                        updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 全局节点模板
CREATE TABLE cfg_node (
                        id         VARCHAR(64)  PRIMARY KEY,
                        name       VARCHAR(128) NOT NULL,
                        type_      VARCHAR(32)  NOT NULL,
                        rule_chain VARCHAR(64),
                        properties JSON,
                        updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 规则链（链头）
CREATE TABLE cfg_rule_chain (
                              id          VARCHAR(64) PRIMARY KEY,
                              name        VARCHAR(128),
                              version     VARCHAR(32),
                              description VARCHAR(256),
                              updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 规则链明细（多对多，带优先级覆盖）
CREATE TABLE cfg_rule_chain_item (
                                   chain_id   VARCHAR(64) NOT NULL,
                                   rule_id    VARCHAR(64) NOT NULL,
                                   priority   INT         NOT NULL,
                                   PRIMARY KEY (chain_id, rule_id),
                                   FOREIGN KEY (chain_id) REFERENCES cfg_rule_chain(id) ON DELETE CASCADE,
                                   FOREIGN KEY (rule_id)  REFERENCES cfg_rule(id)      ON DELETE CASCADE
);

-- 流程链（链头）
CREATE TABLE cfg_flow_chain (
                              id          VARCHAR(64) PRIMARY KEY,
                              name        VARCHAR(128),
                              version     VARCHAR(32),
                              description VARCHAR(256),
                              updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 流程链明细（有序列表）
CREATE TABLE cfg_flow_chain_item (
                                   chain_id   VARCHAR(64) NOT NULL,
                                   seq_no     INT         NOT NULL,
                                   node_id    VARCHAR(64) NOT NULL,
                                   rule_chain VARCHAR(64),
                                   properties JSON,
                                   PRIMARY KEY (chain_id, seq_no),
                                   FOREIGN KEY (chain_id) REFERENCES cfg_flow_chain(id) ON DELETE CASCADE,
                                   FOREIGN KEY (node_id)  REFERENCES cfg_node(id)     ON DELETE CASCADE
);
