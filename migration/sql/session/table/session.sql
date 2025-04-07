-- Таблица: session
CREATE TABLE IF NOT EXISTS session.session (
    id SERIAL PRIMARY KEY,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);