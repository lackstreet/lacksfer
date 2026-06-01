ALTER TABLE transfers
    ADD COLUMN expected_storage_size_bytes BIGINT NOT NULL DEFAULT 1;

ALTER TABLE transfers
    ALTER COLUMN expected_storage_size_bytes DROP DEFAULT;