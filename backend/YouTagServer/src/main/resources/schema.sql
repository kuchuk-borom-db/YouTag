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
    id            VARCHAR(50) PRIMARY KEY, -- Primary key for the video ID
    title         VARCHAR(250),            -- Title of the video
    description   VARCHAR(500),            -- Description of the video
    thumbnail_url VARCHAR(500),            -- URL for the thumbnail
    updated       TIMESTAMP(6) NOT NULL    -- Last updated timestamp (non-nullable)
);

create table if not exists junction
(
    user_id  VARCHAR(255) NOT NULL,
    video_id VARCHAR(255) NOT NULL,
    tag      VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, video_id), -- Composite primary key
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX junction_tag_index ON junction (tag);


