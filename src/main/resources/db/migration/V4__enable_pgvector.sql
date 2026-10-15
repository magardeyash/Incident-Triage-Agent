CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS runbook_documents (
    id BIGSERIAL PRIMARY KEY,
    document_id VARCHAR(100) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    chunk_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS vector_store (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content text NOT NULL,
    metadata jsonb,
    embedding vector(768)
);

CREATE INDEX IF NOT EXISTS vector_store_embedding_idx ON vector_store USING HNSW (embedding vector_cosine_ops);
CREATE INDEX IF NOT EXISTS idx_runbook_docs_doc_id ON runbook_documents(document_id);
