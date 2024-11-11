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

create table if not exists user_videos
(
    id       VARCHAR(255) PRIMARY KEY,
    user_id  VARCHAR(250) NOT NULL,
    video_id VARCHAR(50)  NOT NULL,
    unique (user_id, video_id)
);

-- Removed: create index if not exists idx_user_videos_user on user_videos (user_id);
-- Reason: Redundant because idx_user_videos_composite (user_id, video_id) covers this due to leftmost prefix rule.
-- Any query using just user_id can use the composite index.

create index if not exists idx_user_videos_video on user_videos (video_id);
create index if not exists idx_user_videos_composite on user_videos (user_id, video_id);

create table if not exists junction
(
    id       VARCHAR(255) PRIMARY KEY,
    user_id  VARCHAR(255) NOT NULL,
    video_id VARCHAR(255) NOT NULL,
    tag      VARCHAR(255) NOT NULL,
    unique (user_id, video_id, tag)
);

create index if not exists idx_junction_user_video_user_video_tag on junction (user_id, video_id, tag);
create index if not exists idx_junction_user_tag ON junction (user_id, tag);

-- Not used index: create index if not exists idx_junction_user_id ON junction (user_id);
-- Reason: Redundant because both remaining indexes start with user_id.
-- Queries using just user_id can use either of the remaining indexes due to leftmost prefix rule.

-- Not use index: create index if not exists idx_junction_user_video ON junction (user_id, video_id);
-- Reason: Redundant because idx_junction_user_video_user_video_tag (user_id, video_id, tag) covers this.
-- Queries using user_id + video_id can use the triple composite index due to leftmost prefix rule.

/* Index Coverage for Common Queries:
1. Find by user_id:
   - Uses prefix of idx_junction_user_video_user_video_tag or idx_junction_user_tag

2. Find by user_id + video_id:
   - Uses prefix of idx_junction_user_video_user_video_tag

3. Find by user_id + tag:
   - Uses idx_junction_user_tag

4. Find by user_id + video_id + tag:
   - Uses full idx_junction_user_video_user_video_tag

5. Find videos by video_id (in user_videos):
   - Uses idx_user_videos_video
*/