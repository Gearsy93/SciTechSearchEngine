-- Таблица: viewed_document
CREATE TABLE IF NOT EXISTS session.viewed_document (
    id SERIAL PRIMARY KEY,
    query_id INTEGER REFERENCES session.query(id) ON DELETE CASCADE,
    document_id text REFERENCES session.search_result(document_id) ON DELETE CASCADE
);