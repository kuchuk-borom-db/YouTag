import {SERVER_URI} from "../../utils/Constants.ts";
import type User from "../../models/User.ts";

export async function getGoogleLoginUrl(): Promise<string | null> {
    const url: string = `${SERVER_URI}/graphql`;
    const response = await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            query: `
                query GetOAuthLoginURL {
                    publicData {
                        getOAuthLoginURL(provider: GOOGLE) {
                            data
                        }
                    }
                }
            `
        })
    });

    if (!response.ok) {
        return null;
    }

    const jsonBody = await response.json();
    console.log(`RESPONSE = ${JSON.stringify(jsonBody)}`)
    return jsonBody.data?.publicData?.getOAuthLoginURL?.data || null;
}

export async function exchangeCodeForToken(code: string): Promise<string | null> {
    const url = `${SERVER_URI}/graphql`;
    const response = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            query: `
                mutation ExchangeOAuthTokenForAccessToken {
                    public {
                        exchangeOAuthTokenForAccessToken(token: "${code}", provider: GOOGLE) {
                            data
                        }
                    }
                }
            `
        })
    });

    if (!response.ok) {
        return null;
    }

    const jsonBody = await response.json();
    return jsonBody.data?.public?.exchangeOAuthTokenForAccessToken?.data || null;
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


export async function deleteProfile(token: string): Promise<void> {
    const url = `${SERVER_URI}/authenticated/auth/user`;
    await fetch(url, {
        headers: {Authorization: `Bearer ${token}`},
        method: "DELETE",
    })
}

function parseJsonDataToUser(jsonData: any): User | null {
    return {
        email: jsonData['email'],
        name: jsonData['name'],
        thumbnailUrl: jsonData['pic'],
    }
}

export const prerender = false