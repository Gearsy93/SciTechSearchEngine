-- Таблица: search_result
CREATE TABLE IF NOT EXISTS session.search_result (
    id SERIAL PRIMARY KEY,
    query_id INTEGER REFERENCES session.query(id) ON DELETE CASCADE,
    document_id text unique not null,
    document_url TEXT NOT NULL,
    title TEXT NOT NULL,
    snippet TEXT,
    score FLOAT
);