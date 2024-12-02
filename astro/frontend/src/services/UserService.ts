import { SERVER_URI } from "../utils/Constants.ts";
import type { User } from "../models/User.ts";

export async function getUserDetails(token: string): Promise<User | null> {
    if (!token) {
        console.log("getUserDetails: No token provided.");
        return null;
    }

    const url = `${SERVER_URI}/authenticated/auth/user`;
    console.log(`getUserDetails: Sending request to ${url} with token`);

    try {
        const response = await fetch(url, {
            headers: {
                "Authorization": `Bearer ${token}`,
            },
        });

        console.log(`getUserDetails: Response received with status ${response.status}`);

        if (response.status !== 200) {
            console.log(`getUserDetails: Failed to fetch user details. Status: ${response.status}`);
            return null;
        }

        const decodedBody = await response.json();
        console.log("getUserDetails: Response body decoded:", decodedBody);

        const userJson = decodedBody['data'];
        return parseUserJsonToUserModel(userJson);
    } catch (error) {
        console.error("getUserDetails: Error during fetch or parsing:", error);
        return null;
    }
}

function parseUserJsonToUserModel(userJson: any): User {
    console.log("parseUserJsonToUserModel: Parsing user data", userJson);

    const userModel: User = {
        email: userJson['email'],
        name: userJson['name'],
        thumbnailUrl: userJson['pic'],
    };

    console.log("parseUserJsonToUserModel: Parsed user model", userModel);

    return userModel;
}
