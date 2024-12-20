import type Video from "../../models/Video.ts";
import {SERVER_URI} from "../../utils/Constants.ts";
import Cookies from 'js-cookie';


interface VideoInfoDTO {
    videoDTO: VideoDTO
    tags: string[]
}

interface VideoDTO {
    id: string
    title: string
    author: string
    authorUrl: string
    thumbnail: string
    updated: any
}

export async function saveVideo(videoId: string): Promise<boolean> {
    console.log("Saving video");
    const token = Cookies.get("token");
    if (!token) {
        console.error("No token found in cookie");
        return false
    }
    const url = `${SERVER_URI}/authenticated/video/${videoId}`;
    const response = await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        }
    })
    if (!response.ok) {
        console.error(`Failed to create video ${JSON.stringify(response)}`);
        return false
    }
    return true;
}


export async function getAllVideos(skip: number, limit: number, token: string): Promise<Video[] | null> {
    const url = `${SERVER_URI}/graphql`;
    const response = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
            query: `
                query getAllVideosOfUserWithPagination {
                    authenticatedData {
                        user {
                            data {
                                videos(skip: ${skip}, limit: ${limit}, contains: []) {
                                    author
                                    authorUrl
                                    id
                                    thumbnail
                                    title
                                    associatedTags(skip: 0, limit: 10) {
                                        name
                                    }
                                }
                            }
                        }
                    }
                }
            `
        })
    });

    if (!response.ok) {
        console.error("Response did not return 200 status");
        console.error(JSON.stringify(response));
        return null;
    }

    const jsonBody = await response.json();
    const videos = jsonBody.data?.authenticatedData?.user?.data?.videos;

    if (!videos) return null;

    return videos.map(video => ({
        videoId: video.id,
        title: video.title,
        thumbnailUrl: video.thumbnail,
        author: video.author,
        authorUrl: video.authorUrl,
        tags: video.associatedTags.map(tag => tag.name)
    }));
}

export async function getVideosWithTags(tags: string[], skip: number, limit: number, token: string): Promise<Video[] | null> {
    console.log(`Getting videos with tags ${tags} skip ${skip} and limit ${limit}`)
    const url = `${SERVER_URI}/authenticated/tag/?tags=${tags.join(',').toLowerCase()}&skip=${skip}&limit=${limit}`;
    const response = await fetch(url, {
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        }
    })

    if (!response.ok) {
        console.error(`Response error ${JSON.stringify(response)}`);
        return null
    }
    const responseJson = await response.json();
    return parseVideosFromData(responseJson['data']);
}

export async function getVideosCountOfUser(token: string): Promise<number | null> {


    const url = `${SERVER_URI}/authenticated/video/count`;
    const response = await fetch(url, {
        method: "GET",
        headers: {"content-type": "application/json", "Authorization": `Bearer ${token}`},
    });
    if (!response.ok) {
        return null;
    }
    const json = await response.json();
    return parseInt(json["data"]);
}

export async function getVideosCountWithTags(tags: string[], token: string): Promise<number | null> {
    const url = `${SERVER_URI}/authenticated/video/count?tags=${tags.join(',').trim().toLowerCase()}`;
    const response = await fetch(url, {
        method: "GET",
        headers: {"content-type": "application/json", "Authorization": `Bearer ${token}`},
    });
    const json = await response.json();
    return parseInt(json["data"]);
}

export async function deleteVideo(videoId: string, token: string): Promise<boolean> {
    console.log(`Deleting video ${videoId}`)
    const url = `${SERVER_URI}/authenticated/video/?videos=${videoId}`;
    const response = await fetch(url, {
        method: "DELETE",
        headers: {"content-type": "application/json", "Authorization": `Bearer ${token}`},
    })
    return response.ok;
}

function parseVideosFromData(data: VideoInfoDTO[]): Video[] {
    const videos: Video[] = [];
    data.forEach((video) => {
        videos.push({
            videoId: video.videoDTO.id,
            title: video.videoDTO.title,
            thumbnailUrl: video.videoDTO.thumbnail,
            author: video.videoDTO.author,
            authorUrl: video.videoDTO.authorUrl,
            tags: video.tags
        })
    })
    return videos;
}


//TODO Combine tags from option for adding videos to skip having to add tags manually one by one. This will introduce search feature for video with suggestions
export const prerender = false