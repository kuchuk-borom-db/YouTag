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
    const query = `
        query {
            authenticatedData {
                user {
                    success
                    message
                    data {
                        email
                        name
                        thumbnail
                    }
                }
            }
        }
    `;

    const response = await fetch(`${SERVER_URI}/graphql`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
            query
        })
    });

    if (!response.ok) {
        console.error(`Response error getUserInfo ${JSON.stringify(response)}`);
        return null;
    }

    const responseJson = await response.json();

    if (responseJson.errors) {
        console.error('GraphQL Errors: getUserInfo', responseJson.errors);
        return null;
    }

    const userData = responseJson.data?.authenticatedData?.user;

    if (!userData?.success || !userData?.data) {
        console.error('User data fetch failed:', userData?.message);
        return null;
    }

    return {
        name: userData.data.name,
        email: userData.data.email,
        thumbnailUrl: userData.data.thumbnail
    };
}

export async function deleteProfile(token: string): Promise<void> {
    const url = `${SERVER_URI}/authenticated/auth/user`;
    await fetch(url, {
        headers: {Authorization: `Bearer ${token}`},
        method: "DELETE",
    })
}


export const prerender = false