
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

export interface AddTagsToVideosInput {
    videoIds: string[];
    tagNames: string[];
}

export interface RemoveTagsFromVideosInput {
    videoIds: string[];
    tagNames: string[];
}

export interface RemoveVideosInput {
    videoIds: string[];
}

export interface ResponseModel {
    success: boolean;
    message?: Nullable<string>;
}

export interface IMutation {
    public(): PublicMutation | Promise<PublicMutation>;
    auth(): AuthMutation | Promise<AuthMutation>;
}

export interface PublicMutation {
    exchangeOAuthTokenForAccessToken?: StringResponse;
}

export interface AuthMutation {
    addTagsToVideos?: ResponseModel;
    removeTagsFromVideos?: ResponseModel;
    removeVideos?: ResponseModel;
}

export interface IQuery {
    publicData(): PublicQuery | Promise<PublicQuery>;
    authenticatedData(): AuthQuery | Promise<AuthQuery>;
}

export interface PublicQuery {
    getOAuthLoginURL?: StringResponse;
}

export interface AuthQuery {
    user: UserResponse;
    tags?: TagsResponse;
    videos?: VideosResponse;
}

export interface StringResponse extends ResponseModel {
    data: string;
    message?: Nullable<string>;
    success: boolean;
}

export interface UserResponse extends ResponseModel {
    success: boolean;
    message?: Nullable<string>;
    data?: Nullable<User>;
}

export interface TagsResponse extends ResponseModel {
    message?: Nullable<string>;
    success: boolean;
    data?: Nullable<Nullable<Tag>[]>;
}

export interface VideosResponse extends ResponseModel {
    message?: Nullable<string>;
    success: boolean;
    data?: Nullable<Nullable<Video>[]>;
}

export interface User {
    name: string;
    email: string;
    thumbnail: string;
    tags?: Nullable<Nullable<Tag>[]>;
}

export interface Tag {
    name: string;
    videosWithTag?: Nullable<Nullable<Video>[]>;
}

export interface Video {
    id: string;
    title: string;
    author: string;
    authorUrl: string;
    thumbnail: string;
    associatedTags?: Nullable<Nullable<Tag>[]>;
}

type Nullable<T> = T | null;
