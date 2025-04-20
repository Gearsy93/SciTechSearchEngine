CREATE TABLE session.viniti_document_rubric_term
(
    id               SERIAL PRIMARY KEY,
    viniti_document_id INTEGER REFERENCES session.viniti_document(id) ON DELETE CASCADE,
    rubric_cipher    VARCHAR(32) NOT NULL,
    keyword          TEXT NOT NULL
);
