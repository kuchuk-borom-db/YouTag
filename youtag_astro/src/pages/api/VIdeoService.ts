import type Video from "../../models/Video.ts";
import {SERVER_URI} from "../../utils/Constants.ts";

export async function getAllVideos(skip: number, limit: number, token: string): Promise<{
    videos: Video[],
    totalCount: number
} | null> {
    // Get the server URI from environment variables
    const serverUri = import.meta.env.PUBLIC_SERVER_URI || 'http://localhost:3000';
    const url = `${serverUri}/graphql`;

    // Add retry logic and timeout
    const fetchWithRetry = async (attempt = 1, maxAttempts = 3) => {
        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    query: `
                    query Videos($skip: Int!, $limit: Int!) {
                        authenticatedData {
                            user {
                                data {
                                    videos(skip: $skip, limit: $limit, contains: []) {
                                        count
                                        message
                                        success
                                        data {
                                            id
                                            title
                                            author
                                            authorUrl
                                            thumbnail
                                            associatedTags(skip: 0, limit: 999) {
                                                count
                                                data {
                                                    name
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }`,
                    variables: { skip, limit }
                }),
                // Add timeout
                signal: AbortSignal.timeout(5000)
            });

            if (!response.ok) {
                console.error(`Failed request (attempt ${attempt}):`, {
                    status: response.status,
                    statusText: response.statusText
                });
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return response;
        } catch (error) {
            if (attempt < maxAttempts) {
                console.log(`Retrying request (attempt ${attempt + 1})...`);
                await new Promise(resolve => setTimeout(resolve, 1000 * attempt));
                return fetchWithRetry(attempt + 1, maxAttempts);
            }
            throw error;
        }
    };

    try {
        const response = await fetchWithRetry();
        const jsonBody = await response.json();
        const videosResponse = jsonBody.data?.authenticatedData?.user?.data?.videos;

        if (!videosResponse || !videosResponse.data) {
            console.error("Invalid response structure:", jsonBody);
            return null;
        }

        const result = {
            videos: videosResponse.data.map((video: {
                id: any;
                title: any;
                thumbnail: any;
                author: any;
                authorUrl: any;
                associatedTags: { data: any[]; };
            }) => ({
                videoId: video.id,
                title: video.title,
                thumbnailUrl: video.thumbnail,
                author: video.author,
                authorUrl: video.authorUrl,
                tags: video.associatedTags.data.map(tag => tag.name)
            })),
            totalCount: videosResponse.count
        };

        return result;
    } catch (e) {
        console.error(`Error at getAllVideos:`, {
            error: e,
            serverUri,
            environment: process.env.NODE_ENV
        });
        return {
            videos: [],
            totalCount: 0
        };
    }
}

export async function getVideosWithTags(tags: string[], skip: number, limit: number, token: string): Promise<{
    videos: Video[],
    totalCount: number
}> {
    console.log(`Getting videos with tags ${tags} skip ${skip} and limit ${limit}`);

    const tagsString = JSON.stringify(tags.map(tag => tag.toLowerCase()));
    const query = `
        query {
            authenticatedData {
                user {
                    data {
                        videos(skip: ${skip}, limit: ${limit}, contains: ${tagsString}) {
                            count
                            data {
                                id
                                title
                                author
                                authorUrl
                                thumbnail
                                associatedTags(skip: 0, limit: 999) {
                                    count
                                    data {
                                        name
                                    }
                                }
                            }
                        }
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
        console.error(`Response error getVideosWithTags ${JSON.stringify(response)}`);
        return {videos: [], totalCount: 0};
    }

    const responseJson = await response.json();

    if (responseJson.errors) {
        console.error('GraphQL Errors: getVideosWithTags', responseJson.errors);
        return {videos: [], totalCount: 0};
    }

    const videosData = responseJson.data?.authenticatedData?.user?.data?.videos;

    if (!videosData) {
        return {
            videos: [],
            totalCount: 0
        };
    }

    const videos: Video[] = videosData.data.map((video: {
        id: any;
        title: any;
        author: any;
        authorUrl: any;
        thumbnail: any;
        associatedTags: { data: any[]; };
    }) => ({
        videoId: video.id,
        title: video.title,
        author: video.author,
        authorUrl: video.authorUrl,
        thumbnailUrl: video.thumbnail,
        tags: video.associatedTags.data.map(tag => tag.name)
    }));

    return {
        videos,
        totalCount: videosData.count
    };
}

export async function deleteVideo(videoId: string, token: string): Promise<boolean> {
    console.log(`Deleting video ${videoId}`);

    const graphqlQuery = {
        query: `
        mutation removeVideos {
          auth {
            removeVideos(input: { videoIds: ["${videoId}"] }) {
              message
              success
            }
          }
        }
        `,
    };

    const response = await fetch(`${SERVER_URI}/graphql`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`,
        },
        body: JSON.stringify(graphqlQuery),
    });

    if (!response.ok) {
        console.error(`Failed to delete video: ${response.statusText}`);
        return false;
    }

    const result = await response.json();
    const success = result?.data?.auth?.removeVideos?.success;

    if (success) {
        console.log(`Video ${videoId} deleted successfully.`);
        return true;
    } else {
        console.error(`Failed to delete video: ${result?.data?.auth?.removeVideos?.message}`);
        return false;
    }
}


//TODO Combine tags from option for adding videos to skip having to add tags manually one by one. This will introduce search feature for video with suggestions
export const prerender = false