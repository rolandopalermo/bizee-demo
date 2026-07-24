-- Keep identity sequences in sync after explicit seed IDs in V1.
SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT COALESCE(MAX(id), 1) FROM users));
SELECT setval(pg_get_serial_sequence('registered_agents', 'id'), (SELECT COALESCE(MAX(id), 1) FROM registered_agents));
SELECT setval(pg_get_serial_sequence('companies', 'id'), (SELECT COALESCE(MAX(id), 1) FROM companies));
