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
    user_id  VARCHAR(250) NOT NULL,
    video_id VARCHAR(50)  NOT NULL,
    unique (user_id, video_id)
);
/*
 Holds which users have which userTag. One to many again. One use can have many userTags. Correct me if i am wrong
 */
create table if not exists user_tags
(
    user_id VARCHAR(255) NOT NULL,
    tag      VARCHAR(255) NOT NULL,
    unique(user_id, userTag)
);
/*
 Holds which user's video has which userTag
 */
create table if not exists user_video_tag (
    user_id VARCHAR(255) NOT NULL,
    video_id VARCHAR(255) NOT NULL,
    tag_id VARCHAR(255) NOT NULL,
    unique(user_id,video_id,tag_id)
);
