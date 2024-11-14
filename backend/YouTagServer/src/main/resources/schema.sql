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

create table if not exists users
(
    id            varchar(250) not null
        primary key,
    thumbnail_url varchar(250) not null,
    updated       timestamp(6) not null,
    name          varchar(250) not null
);

create table if not exists videos
(
    id            VARCHAR(50) PRIMARY KEY,
    title         VARCHAR(250),
    description   VARCHAR(500),
    thumbnail_url VARCHAR(500),
    updated       TIMESTAMP(6) NOT NULL
);

create index if not exists idx_videos_title on videos(title);

create table if not exists user_video
(
    id       VARCHAR(255) PRIMARY KEY,
    user_id  VARCHAR(250) NOT NULL,
    video_id VARCHAR(50)  NOT NULL,
    unique (user_id, video_id)
);

create index if not exists idx_user_videos_video on user_video (video_id);
create index if not exists idx_user_videos_composite on user_video (user_id, video_id);

create table if not exists tags
(
    id       VARCHAR(255) PRIMARY KEY,
    user_id  VARCHAR(255) NOT NULL,
    video_id VARCHAR(255) NOT NULL,
    tag      VARCHAR(255) NOT NULL,
    unique (user_id, video_id, tag)
);

create index if not exists idx_tags_user_video_tag on tags (user_id, video_id, tag);
create index if not exists idx_tags_user_tag ON tags (user_id, tag);

/* Index Coverage for Common Queries:
1. Find by user_id:
   - Uses prefix of idx_tags_user_video_tag or idx_tags_user_tag

2. Find by user_id + video_id:
   - Uses prefix of idx_tags_user_video_tag

3. Find by user_id + tag:
   - Uses idx_tags_user_tag

4. Find by user_id + video_id + tag:
   - Uses full idx_tags_user_video_tag

5. Find videos by video_id (in user_videos):
   - Uses idx_user_videos_video
*/