import {SERVER_URI} from "../utils/Constants.ts";
import type {User} from "../models/User.ts";

export async function getUserDetails(Astro: any): Promise<User | null> {
    const jwtToken = Astro.cookies.get("token")?.value;
    if (!jwtToken) return null;

    const url = `${SERVER_URI}/authenticated/auth/user`
    const response = await fetch(url, {
        headers: {
            "Authorization": `Bearer ${jwtToken}`
        },
    })

    if (response.status != 200) {
        return null;
    }
    const decodedBody = await response.json();
    const userJson = decodedBody['data'];
    return parseUserJsonToUserModel(userJson);
}

function parseUserJsonToUserModel(userJson: any): User {
    return {
        email: userJson['email'],
        name: userJson['name'],
        thumbnailUrl: userJson['pic']
    };
}