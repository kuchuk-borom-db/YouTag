
/*
 * -------------------------------------------------------
 * THIS FILE WAS AUTOMATICALLY GENERATED (DO NOT MODIFY)
 * -------------------------------------------------------
 */

/* tslint:disable */
/* eslint-disable */

export enum OAUTH_PROVIDER {
    GOOGLE = "GOOGLE"
}

export class AddTagsToVideosInput {
    videoIds: string[];
    tagNames: string[];
}

export class RemoveTagsFromVideosInput {
    videoIds: string[];
    tagNames: string[];
}

export class RemoveVideosInput {
    videoIds: string[];
}

export interface ResponseModel {
    success: boolean;
    message?: Nullable<string>;
}

export abstract class IMutation {
    abstract public(): PublicMutation | Promise<PublicMutation>;

    abstract auth(): AuthMutation | Promise<AuthMutation>;
}

export class PublicMutation {
    exchangeOAuthTokenForAccessToken?: StringResponse;
}

export class AuthMutation {
    addTagsToVideos?: NoDataResponse;
    removeTagsFromVideos?: NoDataResponse;
    removeVideos?: NoDataResponse;
}

export abstract class IQuery {
    abstract publicData(): PublicQuery | Promise<PublicQuery>;

    abstract authenticatedData(): AuthQuery | Promise<AuthQuery>;
}

export class PublicQuery {
    getOAuthLoginURL?: StringResponse;
}

export class AuthQuery {
    user: UserResponse;
    tags?: TagsResponse;
    videos?: VideosResponse;
}

export class NoDataResponse implements ResponseModel {
    message?: Nullable<string>;
    success: boolean;
}

export class StringResponse implements ResponseModel {
    data: string;
    message?: Nullable<string>;
    success: boolean;
}

export class UserResponse implements ResponseModel {
    success: boolean;
    message?: Nullable<string>;
    data?: Nullable<User>;
}

export class TagsResponse implements ResponseModel {
    message?: Nullable<string>;
    success: boolean;
    data?: Nullable<Nullable<Tag>[]>;
}

export class VideosResponse implements ResponseModel {
    message?: Nullable<string>;
    success: boolean;
    data?: Nullable<Nullable<Video>[]>;
}

export class User {
    name: string;
    email: string;
    thumbnail: string;
    tags?: Nullable<Tag[]>;
    videos?: Nullable<Video[]>;
}

export class Tag {
    name: string;
    videosWithTag?: Nullable<Video[]>;
}

export class Video {
    id: string;
    title: string;
    author: string;
    authorUrl: string;
    thumbnail: string;
    associatedTags?: Nullable<Tag[]>;
}

type Nullable<T> = T | null;
