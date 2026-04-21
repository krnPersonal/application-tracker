ALTER TABLE users
    ADD COLUMN resume_file_name VARCHAR(255) NULL,
    ADD COLUMN resume_content_type VARCHAR(120) NULL,
    ADD COLUMN resume_data LONGBLOB NULL;
