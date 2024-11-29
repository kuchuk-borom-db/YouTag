import {SERVER_URI} from "../utils/Constants.ts";

export async function getGoogleLoginUrl(): Promise<string | null> {
    const url: string = `${SERVER_URI}/public/auth/login/google`;
    const response = await fetch(url);
    if (response.status != 200)
        return null;
    const jsonBody = await response.json();
    return jsonBody['data'];
}