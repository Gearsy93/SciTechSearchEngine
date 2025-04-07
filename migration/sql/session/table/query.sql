-- Таблица: query
CREATE TABLE IF NOT EXISTS session.query (
    id SERIAL PRIMARY KEY,
    session_id INTEGER REFERENCES session.session(id) ON DELETE CASCADE,
    query_text TEXT NOT NULL,
    iteration INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);