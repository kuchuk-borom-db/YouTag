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
    thumbnail   VARCHAR(250) NOT NULL,
    updated     TIMESTAMP    NOT NULL
);

CREATE TABLE user_video
(
    user_id  VARCHAR(100),
    video_id VARCHAR(50),
    created  TIMESTAMP,
    PRIMARY KEY (user_id, video_id),
    FOREIGN KEY (user_id) REFERENCES users (email),
    FOREIGN KEY (video_id) REFERENCES videos (id)
);

CREATE TABLE video_tag
(
    video_id VARCHAR(50),
    tag      VARCHAR(100),
    created  TIMESTAMP,
    PRIMARY KEY (video_id, tag),
    FOREIGN KEY (video_id) REFERENCES videos (id)
);

CREATE INDEX idx_video_tag_tag ON video_tag (tag);

--Operations
-- Get videos of user
-- Get videos of user containing tag(s)
-- Get videos with tag(s)