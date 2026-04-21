CREATE TABLE study_topics (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    title VARCHAR(160) NOT NULL,
    status VARCHAR(40) NOT NULL,
    notes TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_study_topics_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_study_topics_course FOREIGN KEY (course_id) REFERENCES study_courses(id)
);
