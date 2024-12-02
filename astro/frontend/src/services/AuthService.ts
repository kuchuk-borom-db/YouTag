import {SERVER_URI} from "../utils/Constants.ts";

export async function getGoogleLoginUrl(): Promise<string | null> {
    const url: string = `${SERVER_URI}/public/auth/login/google`;
    const response = await fetch(url);
    if (response.status != 200)
        return null;
    const jsonBody = await response.json();
    return jsonBody['data'];
}

export async function getJwtToken(code: string, state: string): Promise<string | null> {

    try {
        const url = `${SERVER_URI}/public/auth/redirect/google?code=${code}&state=${state}`;
        console.log(`Getting token from url ${url}`)
        const resp = await fetch(url);
        if (resp.status != 200)
            return null;
        const jsonBody = await resp.json();
        console.log(`Got token ${jsonBody['data']}`)
        return jsonBody['data'];
    } catch (e) {
        return null;
    }

}

