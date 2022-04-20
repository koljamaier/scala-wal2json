CREATE ROLE root REPLICATION LOGIN;
CREATE DATABASE test;
CREATE TABLE IF NOT EXISTS test_table
(
    id   text NOT NULL,
    code text,
    PRIMARY KEY (id)
);