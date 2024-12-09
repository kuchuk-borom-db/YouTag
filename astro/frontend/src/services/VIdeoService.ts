import type Video from "../models/Video.ts";
import {SERVER_URI} from "../utils/Constants.ts";
import Cookies from 'js-cookie';


interface VideoInfoDTO {
    videoDTO: VideoDTO
    tags: string[]
}

interface VideoDTO {
    id: string
    title: string
    description: string
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

    const url = `${SERVER_URI}/authenticated/video/?skip=${parseInt(String(skip))}&limit=${parseInt(String(limit))}`;
    const response = await fetch(url, {
        headers: new Headers({
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        }),
    });

    if (!response.ok) {
        console.error("Response did not return 200 status")
        console.error(JSON.stringify(response));
        return null
    }

    const responseJson = await response.json();
    const data: VideoInfoDTO[] = responseJson['data']
    return parseVideosFromData(data)

}

export async function   getVideosWithTags(tags: string[], skip: number, limit: number, token : string): Promise<Video[] | null> {
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

export async function getVideosCountOfUser(token : string): Promise<number | null> {


    const url = `${SERVER_URI}/authenticated/video/count`;
    const response = await fetch(url, {
        method: "GET",
        headers: {"content-type": "application/json", "Authorization": `Bearer ${token}`},
    });
    const json = await response.json();
    return parseInt(json["data"]);
}

export async function getVideosCountWithTags(tags: string[], token : string): Promise<number | null> {
    const url = `${SERVER_URI}/authenticated/video/count?tags=${tags.join(',').trim().toLowerCase()}`;
    const response = await fetch(url, {
        method: "GET",
        headers: {"content-type": "application/json", "Authorization": `Bearer ${token}`},
    });
    const json = await response.json();
    return parseInt(json["data"]);
}


function parseVideosFromData(data: VideoInfoDTO[]): Video[] {
    const videos: Video[] = [];
    data.forEach((video) => {
        videos.push({
            videoId: video.videoDTO.id,
            title: video.videoDTO.title,
            thumbnailUrl: video.videoDTO.thumbnail,
            description: video.videoDTO.description,
            tags: video.tags
        })
    })
    return videos;
}

//TODO Combine tags from option for adding videos to skip having to add tags manually one by one. This will introduce search feature for video with suggestions