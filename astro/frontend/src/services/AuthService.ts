import {SERVER_URI} from "../utils/Constants.ts";
import type User from "../models/User.ts";

export async function getGoogleLoginUrl(): Promise<string | null> {
    const url: string = `${SERVER_URI}/public/auth/login/google`;
    const response = await fetch(url);
    if (response.status != 200)
        return null;
    const jsonBody = await response.json();
    return jsonBody['data'];
}

export async function getUserInfo(token: string): Promise<User | null> {
    const url = `${SERVER_URI}/authenticated/auth/user`;
    const response = await fetch(url, {
        headers: {Authorization: `Bearer ${token}`},
    })
    if (response.status != 200) return null;
    const jsonBody = await response.json();
    return parseJsonDataToUser(jsonBody['data']);
}


function parseJsonDataToUser(jsonData: any): User | null {
    return {
        email: jsonData['email'],
        name: jsonData['name'],
        thumbnailUrl: jsonData['pic'],
    }
}