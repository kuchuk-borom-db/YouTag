import type {APIRoute} from "astro";
import {deleteProfile} from "./AuthService.ts";

export const DELETE: APIRoute = async (context) => {
    console.log("cookie Delete user account");
    const token = context.cookies.get("token")?.value!;
    await deleteProfile(token);
    context.cookies.delete("token", {
        httpOnly: false,
        path: "/"
    });
    //This will set the cookie in server side
    return new Response("Deleted", {
        status: 200,
    });
}
export const prerender = false;