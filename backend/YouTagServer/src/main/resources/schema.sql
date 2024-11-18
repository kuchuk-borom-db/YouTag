/*
 Spring modulith table for persist event
 */
create table if not exists event_publication
(
    id               uuid not null
        primary key,
    completion_date  timestamp(6) with time zone,
    event_type       text,
    listener_id      text,
    publication_date timestamp(6) with time zone,
    serialized_event text
);
/*
 Holds user info
 */
create table if not exists users
(
    id            varchar(250) not null
        primary key,
    thumbnail_url varchar(250) not null,
    updated       timestamp(6) not null,
    name          varchar(250) not null
);
/*
 Holds video info
 */
create table if not exists videos
(
    id            VARCHAR(50) PRIMARY KEY,
    title         VARCHAR(250),
    description   VARCHAR(500),
    thumbnail_url VARCHAR(500),
    updated       TIMESTAMP(6) NOT NULL
);
/*
 Holds which video is saved for which user. One to many relation. One user can have multiple videos saved. Correct me if i am wrong about one to many
 */
create table if not exists user_video
(
    id       VARCHAR(255) PRIMARY KEY,
    user_id  VARCHAR(250) NOT NULL,
    video_id VARCHAR(50)  NOT NULL,
    unique (user_id, video_id)
);
/*
 Holds which users have which tag. One to many again. One use can have many tags. Correct me if i am wrong
 */
create table if not exists user_tags
(
    id       VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    tag      VARCHAR(255) NOT NULL,
    unique(user_id, tag)
);
/*
 Holds which user's video has which tag
 */
create table if not exists user_video_tag (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    video_id VARCHAR(255) NOT NULL,
    tag_id VARCHAR(255) NOT NULL,
    unique(user_id,video_id,tag_id)
);

-- Users table indexes
-- Purpose: Improve search and filtering by name
CREATE INDEX IF NOT EXISTS idx_users_name ON users(name);

-- Videos table indexes
-- Purpose: Enable fast text search on video titles and filter by update time
CREATE INDEX IF NOT EXISTS idx_videos_title ON videos(title);
CREATE INDEX IF NOT EXISTS idx_videos_updated ON videos(updated);
-- Enable fast lookup of videos for a specific user
CREATE INDEX IF NOT EXISTS idx_user_video_user_video ON user_video(user_id, video_id);

-- User-Video table indexes
-- Purpose: Optimize retrieval of videos for a specific user and vice versa
CREATE INDEX IF NOT EXISTS idx_user_video_user_id ON user_video(user_id);
CREATE INDEX IF NOT EXISTS idx_user_video_video_id ON user_video(video_id);

-- User Tags table indexes
-- Purpose: Fast lookup of tags by user and searching tags
-- Get/Delete tags of a user
CREATE INDEX IF NOT EXISTS idx_user_tags_user_id ON user_tags(user_id);
-- Get/Delete user using a tag
CREATE INDEX IF NOT EXISTS idx_user_tags_tag ON user_tags(tag);
-- Get/Delete specific tag of a user
CREATE INDEX IF NOT EXISTS idx_user_tags_user_tag ON user_tags(user_id, tag);

-- User Video Tag table indexes
-- Get/Delete all tags&vids of user
CREATE INDEX IF NOT EXISTS idx_user_video_tag_user_id ON user_video_tag(user_id);
-- Get/Delete all tag&vids with vid for ALL user
CREATE INDEX IF NOT EXISTS idx_user_video_tag_video_id ON user_video_tag(video_id);
-- Get/Delete all tag&vids using tag for ALL user
CREATE INDEX IF NOT EXISTS idx_user_video_tag_tag_id ON user_video_tag(tag_id);
-- Get/Delete all tags used in specific video of a user
CREATE INDEX IF NOT EXISTS idx_user_video_tag_video_user ON user_video_tag( user_id,video_id);
-- Get/Delete all tag&vids using tag T of user U
CREATE INDEX IF NOT EXISTS idx_user_video_tag_user_tag ON user_video_tag(user_id, tag_id);
