import {SERVER_URI} from "../../utils/Constants.ts";
import Cookies from "js-cookie";

interface AddTagsResponse {
    auth: {
        addTagsToVideos: {
            success: boolean;
            message: string;
        }
    }
}

export const addTagsToVideo = async (videoIds: string[], tags: string[]): Promise<boolean> => {
    const token = Cookies.get("token");
    if (!token) {
        console.log("No Token found in cookie");
        return false;
    }

    console.log(`Adding tags ${tags} to videos ${videoIds}`)

    const query = `
        mutation AddTagsToVideos {
            auth {
                addTagsToVideos(input: {
                    tagNames: ${JSON.stringify(tags.map(tag => tag.trim().toLowerCase()))},
                    videoIds: ${JSON.stringify(videoIds.map(id => id.trim()))}
                }) {
                    success
                    message
                }
            }
        }
    `;

    try {
        const url = `${SERVER_URI}/graphql`;
        const response = await fetch(url, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({query})
        });

        if (!response.ok) {
            console.error("Failed to add tags to video", response.status);
            return false;
        }

        const responseData = await response.json();

        if (responseData.errors) {
            console.error("GraphQL errors:", responseData.errors);
            return false;
        }

        const result = responseData.data?.auth?.addTagsToVideos;
        if (!result) {
            console.error("No response data from mutation");
            return false;
        }

        if (!result.success) {
            console.error("Failed to add tags:", result.message);
            return false;
        }

        return true;
    } catch (error) {
        console.error("Error adding tags to video:", error);
        return false;
    }
}


export async function getAllTags(
    skip: number = 0,
    limit: number = 10
): Promise<TagSearchResult | null> {
    const token = Cookies.get("token");
    if (!token) {
        console.log("No Token found in cookie");
        return null;
    }

    const query = `
        query getAllTags {
            authenticatedData {
                user {
                    data {
                        tags(skip: ${skip}, limit: ${limit}) {
                            count
                            data {
                                name
                            }
                        }
                    }
                }
            }
        }
    `;

    try {
        const url = `${SERVER_URI}/graphql`;
        const response = await fetch(url, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({query})
        });

        if (!response.ok) {
            console.log("Failed to get tags from server", response.status);
            return null;
        }

        const responseData = await response.json();

        if (responseData.errors) {
            console.log("GraphQL errors:", responseData.errors);
            return null;
        }

        const tagsData = responseData.data?.authenticatedData?.user?.data?.tags;
        if (!tagsData) {
            console.log("No tags found in response");
            return null;
        }

        console.log(`All tags = ${tagsData.data.map(tag => tag.name)}`);

        return {
            count: tagsData.count,
            tags: tagsData.data.map(tag => tag.name)
        };
    } catch (error) {
        console.log("Error fetching tags:", error);
        return null;
    }
}

interface TagSearchResult {
    count: number;
    tags: string[];
}

export async function getTagsContainingKeyword(
    keyword: string,
    skip: number = 0,
    limit: number = 5
): Promise<TagSearchResult | null> {
    const token = Cookies.get("token");
    if (!token) {
        console.log("No Token found in cookie");
        return null;
    }

    const query = `
        query getTagsContaining {
            authenticatedData {
                user {
                    data {
                        tags(skip: ${skip}, limit: ${limit}, contains: "${keyword}") {
                            count
                            data {
                                name
                            }
                        }
                    }
                }
            }
        }
    `;

    try {
        const url = `${SERVER_URI}/graphql`;
        const response = await fetch(url, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({query})
        });

        if (!response.ok) {
            console.error("Failed to get tags from server", response.status);
            return null;
        }

        const responseData = await response.json();

        if (responseData.errors) {
            console.error("GraphQL errors:", responseData.errors);
            return null;
        }

        const tagsData = responseData.data?.authenticatedData?.user?.data?.tags;
        if (!tagsData) {
            console.error("No tags found in response");
            return null;
        }

        return {
            count: tagsData.count,
            tags: tagsData.data.map(tag => tag.name)
        };
    } catch (error) {
        console.error("Error fetching tags:", error);
        return null;
    }
}


export async function deleteTagFromVideo(tags: string[], videoIds: string[], token: string): Promise<void> {
    const url = `${SERVER_URI}/graphql`;
    const query = `
        mutation removeTagsFromVideos {
            auth {
                removeTagsFromVideos(input: {
                    tagNames: ${JSON.stringify(tags)},
                    videoIds: ${JSON.stringify(videoIds)}
                }) {
                    message
                    success
                }
            }
        }
    `;

    const response = await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({query})
    });

    const data = await response.json();

    if (data.data?.auth?.removeTagsFromVideos?.success) {
        console.log("Deleted tags successfully");
    } else {
        console.error("Failed to delete tags:", data.data?.auth?.removeTagsFromVideos?.message);
        throw new Error(data.data?.auth?.removeTagsFromVideos?.message || "Failed to delete tags");
    }
}

export const prerender = false