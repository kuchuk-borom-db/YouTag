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
    name          varchar(250) not null,
    thumbnail_url varchar(250) not null,
    updated       timestamp(6) not null
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
 Holds which video is saved for which user
 */
create table if not exists user_video
(
    user_id  VARCHAR(250) NOT NULL,
    video_id VARCHAR(50)  NOT NULL,
    primary key (user_id, video_id)
    );

/*
 Get/Delete video of user
 */
create index if not exists idx_user_video_user_video on user_video(user_id, video_id);
/*
 Get/Delete all videos of user
 */
create index if not exists idx_user_video_user on user_video(user_id);
/*
 Get/Delete unused videos
 */
create index if not exists idx_user_video_video on user_video(video_id);

/*
 Holds which users have which userTag
 */
create table if not exists user_tag
(
    user_id VARCHAR(255) NOT NULL,
    tag     VARCHAR(255) NOT NULL,
    primary key (user_id, tag)
    );

/*
 Get/Delete tags of user operation
 */
create index if not exists idx_user_tag_user on user_tag(user_id);
/*
 Get delete entries with userID and tag
 */
create index if not exists idx_user_tag_user_tag on user_tag(user_id, tag);
/*
 Get/Delete tags of all users
 */
create index if not exists idx_user_tag_tag on user_tag(tag);

/*
 Holds which user's video has which userTag
 */
create table if not exists user_video_tag (
                                              user_id  VARCHAR(255) NOT NULL,
    video_id VARCHAR(255) NOT NULL,
    tag      VARCHAR(255) NOT NULL,
    primary key (user_id, video_id, tag)
    );

/*
 Get tags of video
 */
create index if not exists idx_user_video_tag_user_video on user_video_tag(user_id, video_id);
/*
 Get existence of specific tag
 */
create index if not exists idx_user_video_tag_user_video_tag on user_video_tag(user_id, video_id, tag);
/*
 get videos using the tag
 */
create index if not exists idx_user_video_tag_user_tag on user_video_tag(user_id, tag);