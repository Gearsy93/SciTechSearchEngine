-- Таблица: yandex_document
CREATE TABLE session.yandex_document (
     id           BIGSERIAL PRIMARY KEY,
     query_id     BIGINT NOT NULL REFERENCES session.query(id) ON DELETE CASCADE,
     document_id  VARCHAR(64) NOT NULL,
     title        TEXT NOT NULL,
     link         TEXT NOT NULL
);
