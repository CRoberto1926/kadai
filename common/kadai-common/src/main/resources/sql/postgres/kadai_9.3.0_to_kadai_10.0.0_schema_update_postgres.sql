SET SCHEMA %schemaName%;

ALTER TABLE TASK ADD COLUMN IS_REOPENED BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE TASK SET IS_REOPENED=FALSE;

INSERT INTO KADAI_SCHEMA_VERSION (ID, VERSION, CREATED)
VALUES (nextval('KADAI_SCHEMA_VERSION_ID_SEQ'), '10.0.0', CURRENT_TIMESTAMP);