package com.zornflow.infrastructure.config.source.database;

import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.time.LocalDateTime;

/**
 * 数据库表定义
 * 简化的JOOQ表模型定义，用于数据库操作
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
public class Tables {

  // ============================= 公共规则表 =============================
  public static final Table<?> PUBLIC_RULES = DSL.table("public_rules");

  public static final Field<String> PUBLIC_RULES_ID = DSL.field("id", String.class);
  public static final Field<String> PUBLIC_RULES_NAME = DSL.field("name", String.class);
  public static final Field<Integer> PUBLIC_RULES_PRIORITY = DSL.field("priority", Integer.class);
  public static final Field<String> PUBLIC_RULES_CONDITION = DSL.field("condition_", String.class);
  public static final Field<String> PUBLIC_RULES_HANDLE_TYPE = DSL.field("handle_type", String.class);
  public static final Field<String> PUBLIC_RULES_HANDLER = DSL.field("handler", String.class);
  public static final Field<String> PUBLIC_RULES_PARAMETERS = DSL.field("parameters", String.class);
  public static final Field<LocalDateTime> PUBLIC_RULES_CREATED_AT = DSL.field("created_at", LocalDateTime.class);
  public static final Field<LocalDateTime> PUBLIC_RULES_UPDATED_AT = DSL.field("updated_at", LocalDateTime.class);

  // ============================= 公共节点表 =============================
  public static final Table<?> PUBLIC_NODES = DSL.table("public_nodes");

  public static final Field<String> PUBLIC_NODES_ID = DSL.field("id", String.class);
  public static final Field<String> PUBLIC_NODES_NAME = DSL.field("name", String.class);
  public static final Field<String> PUBLIC_NODES_NODE_TYPE = DSL.field("node_type", String.class);
  public static final Field<String> PUBLIC_NODES_RULE_CHAIN = DSL.field("rule_chain", String.class);
  public static final Field<String> PUBLIC_NODES_PROPERTIES = DSL.field("properties", String.class);
  public static final Field<LocalDateTime> PUBLIC_NODES_CREATED_AT = DSL.field("created_at", LocalDateTime.class);
  public static final Field<LocalDateTime> PUBLIC_NODES_UPDATED_AT = DSL.field("updated_at", LocalDateTime.class);

  // ============================= 规则链表 =============================
  public static final Table<?> RULE_CHAINS = DSL.table("rule_chains");

  public static final Field<String> RULE_CHAINS_ID = DSL.field("id", String.class);
  public static final Field<String> RULE_CHAINS_NAME = DSL.field("name", String.class);
  public static final Field<String> RULE_CHAINS_VERSION = DSL.field("version", String.class);
  public static final Field<String> RULE_CHAINS_DESCRIPTION = DSL.field("description", String.class);
  public static final Field<Boolean> RULE_CHAINS_IS_ACTIVE = DSL.field("is_active", Boolean.class);
  public static final Field<LocalDateTime> RULE_CHAINS_CREATED_AT = DSL.field("created_at", LocalDateTime.class);
  public static final Field<LocalDateTime> RULE_CHAINS_UPDATED_AT = DSL.field("updated_at", LocalDateTime.class);

  // ============================= 规则链规则关联表 =============================
  public static final Table<?> RULE_CHAIN_RULES = DSL.table("rule_chain_rules");

  public static final Field<Long> RULE_CHAIN_RULES_ID = DSL.field("id", Long.class);
  public static final Field<String> RULE_CHAIN_RULES_RULE_CHAIN_ID = DSL.field("rule_chain_id", String.class);
  public static final Field<String> RULE_CHAIN_RULES_RULE_ID = DSL.field("rule_id", String.class);
  public static final Field<String> RULE_CHAIN_RULES_RULE_NAME = DSL.field("rule_name", String.class);
  public static final Field<Integer> RULE_CHAIN_RULES_PRIORITY = DSL.field("priority", Integer.class);
  public static final Field<String> RULE_CHAIN_RULES_CONDITION = DSL.field("condition_", String.class);
  public static final Field<String> RULE_CHAIN_RULES_HANDLE_TYPE = DSL.field("handle_type", String.class);
  public static final Field<String> RULE_CHAIN_RULES_HANDLER = DSL.field("handler", String.class);
  public static final Field<String> RULE_CHAIN_RULES_PARAMETERS = DSL.field("parameters", String.class);
  public static final Field<Boolean> RULE_CHAIN_RULES_IS_PUBLIC_REF = DSL.field("is_public_ref", Boolean.class);
  public static final Field<LocalDateTime> RULE_CHAIN_RULES_CREATED_AT = DSL.field("created_at", LocalDateTime.class);
  public static final Field<LocalDateTime> RULE_CHAIN_RULES_UPDATED_AT = DSL.field("updated_at", LocalDateTime.class);

  // ============================= 流程链表 =============================
  public static final Table<?> PROCESS_CHAINS = DSL.table("process_chains");

  public static final Field<String> PROCESS_CHAINS_ID = DSL.field("id", String.class);
  public static final Field<String> PROCESS_CHAINS_NAME = DSL.field("name", String.class);
  public static final Field<String> PROCESS_CHAINS_VERSION = DSL.field("version", String.class);
  public static final Field<String> PROCESS_CHAINS_DESCRIPTION = DSL.field("description", String.class);
  public static final Field<Boolean> PROCESS_CHAINS_IS_ACTIVE = DSL.field("is_active", Boolean.class);
  public static final Field<LocalDateTime> PROCESS_CHAINS_CREATED_AT = DSL.field("created_at", LocalDateTime.class);
  public static final Field<LocalDateTime> PROCESS_CHAINS_UPDATED_AT = DSL.field("updated_at", LocalDateTime.class);

  // ============================= 流程链节点表 =============================
  public static final Table<?> PROCESS_CHAIN_NODES = DSL.table("process_chain_nodes");

  public static final Field<Long> PROCESS_CHAIN_NODES_ID = DSL.field("id", Long.class);
  public static final Field<String> PROCESS_CHAIN_NODES_PROCESS_CHAIN_ID = DSL.field("process_chain_id", String.class);
  public static final Field<String> PROCESS_CHAIN_NODES_NODE_ID = DSL.field("node_id", String.class);
  public static final Field<String> PROCESS_CHAIN_NODES_NODE_NAME = DSL.field("node_name", String.class);
  public static final Field<String> PROCESS_CHAIN_NODES_NEXT_NODE = DSL.field("next_node", String.class);
  public static final Field<String> PROCESS_CHAIN_NODES_NODE_TYPE = DSL.field("node_type", String.class);
  public static final Field<String> PROCESS_CHAIN_NODES_RULE_CHAIN = DSL.field("rule_chain", String.class);
  public static final Field<String> PROCESS_CHAIN_NODES_PROPERTIES = DSL.field("properties", String.class);
  public static final Field<Boolean> PROCESS_CHAIN_NODES_IS_PUBLIC_REF = DSL.field("is_public_ref", Boolean.class);
  public static final Field<LocalDateTime> PROCESS_CHAIN_NODES_CREATED_AT = DSL.field("created_at", LocalDateTime.class);
  public static final Field<LocalDateTime> PROCESS_CHAIN_NODES_UPDATED_AT = DSL.field("updated_at", LocalDateTime.class);

  // ============================= 网关条件表 =============================
  public static final Table<?> GATEWAY_CONDITIONS = DSL.table("gateway_conditions");

  public static final Field<Long> GATEWAY_CONDITIONS_ID = DSL.field("id", Long.class);
  public static final Field<Long> GATEWAY_CONDITIONS_PROCESS_CHAIN_NODE_ID = DSL.field("process_chain_node_id", Long.class);
  public static final Field<String> GATEWAY_CONDITIONS_CONDITION_EXPR = DSL.field("condition_expr", String.class);
  public static final Field<String> GATEWAY_CONDITIONS_NEXT_NODE_ID = DSL.field("next_node_id", String.class);
  public static final Field<Integer> GATEWAY_CONDITIONS_CONDITION_ORDER = DSL.field("condition_order", Integer.class);
  public static final Field<LocalDateTime> GATEWAY_CONDITIONS_CREATED_AT = DSL.field("created_at", LocalDateTime.class);
}
