-- Таблица: viewed_document
CREATE TABLE IF NOT EXISTS session.viewed_document (
    id SERIAL PRIMARY KEY,
    query_id INTEGER REFERENCES session.query(id) ON DELETE CASCADE,
    document_id INTEGER UNIQUE REFERENCES session.search_result(id) ON DELETE CASCADE
);
