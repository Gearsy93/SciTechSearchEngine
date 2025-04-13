-- Таблица: search_result
CREATE TABLE IF NOT EXISTS session.search_result (
    id SERIAL PRIMARY KEY,
    query_id INTEGER REFERENCES session.query(id) ON DELETE CASCADE,
    document_id TEXT NOT NULL,
    document_url TEXT NOT NULL,
    title TEXT NOT NULL,
    snippet TEXT,
    score FLOAT,
    CONSTRAINT search_result_unique_pair UNIQUE (query_id, document_id)
);
