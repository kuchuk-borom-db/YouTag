import {SERVER_URI} from "../../utils/Constants.ts";
import Cookies from "js-cookie";

export const addTagsToVideo = async (videoId: string[], tags: string[]): Promise<boolean> => {
    const token = Cookies.get("token");
    if (!token) {
        console.log("No Token found in cookie");
        return false;
    }

    const url = `${SERVER_URI}/authenticated/tag/?videos=${videoId.join(',').trim()}&tags=${tags.join(',').trim().toLowerCase()}`;

    const response = await fetch(url, {
        method: "POST",
        headers: {
            "Authorization": `Bearer ${token}`,
        }
    })

    if (!response.ok) {
        console.error(`Failed to add tags to video ${JSON.stringify(response)}`)
        return false;
    }
    return true;
}

export async function getAllTags(skip: number = 0, limit: number = 10): Promise<string[] | null> {
    const token = Cookies.get("token");
    if (!token) {
        console.log("No Token found in cookie");
        return null;
    }
    const url = `${SERVER_URI}/authenticated/tag/?skip=${skip}&limit=${limit}`;
    const response = await fetch(url, {
        method: "GET",
        headers: {
            "Authorization": `Bearer ${token}`,
            "content-type": "application/json"
        }
    })

    if (!response.ok) {
        console.log(`Failed to get tags from server ${JSON.stringify(response)}`)
        return null;
    }
    const respJson = await response.json();
    const tags : string[] = respJson.data;
    console.log(`All tags = ${tags}`)
    return tags;
}

export async function getTagsContainingKeyword(keyword: string, skip: number = 0, limit: number = 5): Promise<string[] | null> {
    const token = Cookies.get("token");
    if (!token) {
        console.log("No Token found in cookie");
        return null;
    }
    const url = `${SERVER_URI}/authenticated/search/tag/${keyword}?skip=${skip}&limit=${limit}`;
    const response = await fetch(url, {
        method: "GET",
        headers: {
            "Authorization": `Bearer ${token}`,
            "content-type": "application/json"
        }
    })
    if (!response.ok) {
        console.error(`Failed to get tags from server ${JSON.stringify(response)}`)
        return null;
    }
    const respJson = await response.json();
    return respJson['data'];
}

export async function getTagCountOfUser(keyword: string = ""): Promise<number | null> {
    console.log("Getting tag count of user ");
    const token = Cookies.get("token");
    if (!token) {
        console.log("No Token found in cookie");
        return null;
    }

    const url = keyword.trim().length <= 0 ? `${SERVER_URI}/authenticated/tag/count` : `${SERVER_URI}/authenticated/search/count/tag/${keyword}`;
    const response = await fetch(url, {
        method: "GET",
        headers: {"content-type": "application/json", "Authorization": `Bearer ${token}`},
    });
    const json = await response.json();

    return Number(json["data"]) || 0;
}

export async function deleteTagFromVideo(tags: string[], videoId: string, token: string): Promise<void> {
    console.log(`Deleting tag ${tags} from video ${videoId}`);
    const url = `${SERVER_URI}/authenticated/tag/?tags=${tags.join(',')}&videos=${videoId}`;
    const response = await fetch(url, {
        method: "DELETE",
        headers: {"content-type": "application/json", "Authorization": `Bearer ${token}`},
    });
    if (response.ok) {
        console.log("Deleted tags successfully")
    } else {
        console.log(`Failed to delete tags from video ${JSON.stringify(response)}}`)
    }
}
export  const prerender = false