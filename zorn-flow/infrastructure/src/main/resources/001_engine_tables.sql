-- 建议在数据库中启用 pgx_ulid 扩展以使用 gen_ulid()
-- CREATE EXTENSION IF NOT EXISTS "pgx_ulid";

SELECT current_database();
SET search_path TO engine;
SHOW search_path;

-- =================================================================
--  Rule Definition Tables
-- =================================================================
-- 共享规则表 (模板)
CREATE TABLE engine.shared_rules
(
  id             VARCHAR(40) PRIMARY KEY      NOT NULL,
  name           VARCHAR(255) NOT NULL,
  priority       INT                   DEFAULT 100,
  condition      TEXT,
  handler_config JSONB,
  created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
COMMENT
ON TABLE shared_rules IS '可复用的共享规则模板';

-- 规则链表
CREATE TABLE engine.rule_chains
(
  id          VARCHAR(40) PRIMARY KEY      NOT NULL,
  name        VARCHAR(255) NOT NULL,
  version     VARCHAR(255),
  description TEXT,
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- 链中的规则实例表
CREATE TABLE engine.chain_rules
(
  id             VARCHAR(40) PRIMARY KEY     NOT NULL,
  rule_chain_id  VARCHAR(40)        NOT NULL REFERENCES rule_chains (id) ,
  shared_rule_id VARCHAR(40)        REFERENCES shared_rules (id) ,
  sequence         INT         NOT NULL,
  name           VARCHAR(255),
  priority       INT,
  condition      TEXT,
  handler_config JSONB,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (rule_chain_id, sequence)
);
COMMENT
ON TABLE chain_rules IS '规则在链中的具体实例，存储顺序、与共享模板的链接以及属性覆盖';

CREATE INDEX idx_chain_rules_rule_chain_id ON chain_rules (rule_chain_id);
CREATE INDEX idx_chain_rules_shared_rule_id ON chain_rules (shared_rule_id);

-- =================================================================
--  Process Definition Tables
-- =================================================================
-- 共享节点表 (模板)
CREATE TABLE engine.shared_nodes
(
  id            VARCHAR(40) PRIMARY KEY      NOT NULL,
  name          VARCHAR(255) NOT NULL,
  node_type     VARCHAR(50)  NOT NULL,
  rule_chain_id VARCHAR(40)         REFERENCES rule_chains (id) , -- FIX: 外键约束
  conditions    JSONB,
  properties    JSONB,
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
COMMENT
ON TABLE shared_nodes IS '可复用的共享流程节点模板';

-- 流程链表
CREATE TABLE engine.process_chains
(
  id          VARCHAR(40) PRIMARY KEY      NOT NULL,
  name        VARCHAR(255) NOT NULL,
  version     VARCHAR(255),
  description TEXT,
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- 链中的节点实例表
CREATE TABLE engine.chain_nodes
(
  id               VARCHAR(40) PRIMARY KEY     NOT NULL,
  process_chain_id VARCHAR(40)        NOT NULL REFERENCES process_chains (id) ,
  shared_node_id   VARCHAR(40)        REFERENCES shared_nodes (id) ,
  sequence         INT         NOT NULL,
  name             VARCHAR(255),
  next_node_id     VARCHAR(40),
  node_type        VARCHAR(50),
  rule_chain_id    VARCHAR(40)        REFERENCES rule_chains (id) , -- FIX: 外键约束
  conditions       JSONB,
  properties       JSONB,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (process_chain_id, sequence)
);
COMMENT
ON TABLE chain_nodes IS '节点在链中的具体实例，存储顺序、与共享模板的链接以及属性覆盖';

CREATE INDEX idx_chain_nodes_process_chain_id ON chain_nodes (process_chain_id);
CREATE INDEX idx_chain_nodes_shared_node_id ON chain_nodes (shared_node_id);
