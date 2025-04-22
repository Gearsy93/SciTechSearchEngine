-- Подключение к схеме session
CREATE SCHEMA IF NOT EXISTS session AUTHORIZATION scisearch;
GRANT ALL ON SCHEMA session TO scisearch;

\i /docker-entrypoint-initdb.d/session/table/session.sql
\i /docker-entrypoint-initdb.d/session/table/query.sql
\i /docker-entrypoint-initdb.d/session/table/search_result.sql
\i /docker-entrypoint-initdb.d/session/table/viewed_document.sql
\i /docker-entrypoint-initdb.d/session/table/viniti_document.sql
\i /docker-entrypoint-initdb.d/session/table/viniti_document_rubric_term.sql
\i /docker-entrypoint-initdb.d/session/table/yandex_document.sql