CREATE TABLE session.viniti_document
(
    id               SERIAL PRIMARY KEY,
    query_id         INTEGER REFERENCES session.query(id) ON DELETE CASCADE,
    title            TEXT NOT NULL,
    annotation       TEXT,
    translate_title  TEXT,
    link             TEXT NOT NULL,
    language         TEXT,
);
