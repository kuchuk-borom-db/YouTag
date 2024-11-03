CREATE TABLE users
(
    email    VARCHAR(100) PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    pic      VARCHAR(250)
);

CREATE TABLE videos
(
    id          VARCHAR(50) PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    thumbnail   VARCHAR(250) NOT NULL
);

CREATE TABLE user_video_tags
(
    user_email VARCHAR(100),
    video_id   VARCHAR(50),
    tags       VARCHAR(50)[],
    PRIMARY KEY (user_email, video_id),
    FOREIGN KEY (user_email) REFERENCES users (email) ON DELETE CASCADE,
    FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE
);

-- Indexes for optimizing queries

-- USER-focused operations
-- For "Get videos of user A"
CREATE INDEX idx_user_video_tags_user_email
    ON user_video_tags (user_email);

-- For "Get videos of user A with tag B"
-- We need a GIN index for the `tags` array to support array-based operations
CREATE INDEX idx_user_video_tags_user_email_tags
    ON user_video_tags (user_email) INCLUDE (tags);

-- VIDEO-focused operations
-- For "get tags of video V" and "get users that have video V"
CREATE INDEX idx_user_video_tags_video_id
    ON user_video_tags (video_id);

-- TAG-focused operations
-- For "get users using tag T" and "get videos using tag T"
-- Using GIN index on the tags array for faster querying
CREATE INDEX idx_user_video_tags_tags
    ON user_video_tags USING GIN (tags);
