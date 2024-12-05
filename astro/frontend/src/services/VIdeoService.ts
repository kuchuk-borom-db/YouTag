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


export async function getAllVideos(skip: number, limit: number): Promise<Video[] | null> {
    const token = Cookies.get("token");
    if (!token) {
        console.error("No token found in cookie");
        return null
    }

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

export async function getVideosCountOfUser(): Promise<number | null> {
    const token = Cookies.get("token");
    if (!token) {
        console.log("No Token found in cookie");
        return null;
    }

    const url = `${SERVER_URI}/authenticated/video/count`;
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