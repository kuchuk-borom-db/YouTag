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
    const serverUri = `${SERVER_URI}/graphql`

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

    try {
        const response = await fetch(serverUri, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ query })
        });

        if (!response.ok) {
            console.error('getUserInfo failed:', {
                status: response.status,
                statusText: response.statusText
            });
            return null;
        }

        const responseJson = await response.json();

        if (responseJson.errors) {
            console.error('GraphQL Errors:', responseJson.errors);
            return null;
        }

        const userData = responseJson.data?.authenticatedData?.user;

        if (!userData?.success || !userData?.data) {
            console.error('User data fetch failed:', {
                message: userData?.message,
                success: userData?.success,
                hasData: !!userData?.data
            });
            return null;
        }

        return {
            name: userData.data.name,
            email: userData.data.email,
            thumbnailUrl: userData.data.thumbnail
        };
    } catch (error) {
        console.error('Failed to fetch user info:', {
            error,
            serverUri,
            environment: process.env.NODE_ENV
        });
        return null;
    }
}

export async function deleteProfile(token: string): Promise<void> {
    const url = `${SERVER_URI}/authenticated/auth/user`;
    await fetch(url, {
        headers: {Authorization: `Bearer ${token}`},
        method: "DELETE",
    });
}

export const prerender = false;
