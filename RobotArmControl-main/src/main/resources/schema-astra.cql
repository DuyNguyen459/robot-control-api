-- ============================================================
-- Robot Arm Control - Astra DB / Cassandra Schema (Query-based)
-- ============================================================
-- NOTE:
-- 1) SimpleStrategy + RF=1 is acceptable only for local/dev/testing.
-- 2) For production use NetworkTopologyStrategy and RF >= 3 per DC,
--    configured in Astra according to your region/multi-region topology.

CREATE KEYSPACE IF NOT EXISTS robot_control
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE robot_control;

CREATE TABLE IF NOT EXISTS robot_logs_by_robot_day (
   robot_id bigint,
   log_date date,
   event_time timestamp,
   event_id timeuuid,
   session_id bigint,
   user_id bigint,
   factory_id bigint,
   log_type text,
   severity text,
   status text,
   command text,
   source text,
   message text,
   trace_id text,
   metadata_json text,
   PRIMARY KEY ((robot_id, log_date), event_time, event_id)
) WITH CLUSTERING ORDER BY (event_time DESC, event_id DESC);

CREATE TABLE IF NOT EXISTS robot_logs_by_session (
   session_id bigint,
   event_time timestamp,
   event_id timeuuid,
   robot_id bigint,
   user_id bigint,
   factory_id bigint,
   log_type text,
   severity text,
   status text,
   command text,
   source text,
   message text,
   trace_id text,
   metadata_json text,
   PRIMARY KEY ((session_id), event_time, event_id)
) WITH CLUSTERING ORDER BY (event_time DESC, event_id DESC);

CREATE TABLE IF NOT EXISTS robot_logs_by_robot_day_type (
   robot_id bigint,
   log_date date,
   log_type text,
   event_time timestamp,
   event_id timeuuid,
   session_id bigint,
   user_id bigint,
   factory_id bigint,
   severity text,
   status text,
   command text,
   source text,
   message text,
   trace_id text,
   metadata_json text,
   PRIMARY KEY ((robot_id, log_date, log_type), event_time, event_id)
) WITH CLUSTERING ORDER BY (event_time DESC, event_id DESC);

CREATE TABLE IF NOT EXISTS robot_alerts_by_robot_day (
   robot_id bigint,
   log_date date,
   severity text,
   event_time timestamp,
   event_id timeuuid,
   session_id bigint,
   user_id bigint,
   factory_id bigint,
   log_type text,
   status text,
   command text,
   source text,
   message text,
   trace_id text,
   metadata_json text,
   PRIMARY KEY ((robot_id, log_date), severity, event_time, event_id)
) WITH CLUSTERING ORDER BY (severity ASC, event_time DESC, event_id DESC);

CREATE TABLE IF NOT EXISTS robot_latest_status (
   robot_id bigint PRIMARY KEY,
   last_event_time timestamp,
   session_id bigint,
   user_id bigint,
   factory_id bigint,
   status text,
   severity text,
   source text,
   message text,
   trace_id text,
   metadata_json text
);


