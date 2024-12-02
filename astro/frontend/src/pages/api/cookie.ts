import type {APIRoute} from "astro";
export const prerender = false; //Required to make this server only file
/**
 * Pass key and value as payload
 */
export const POST: APIRoute = async ({request}) => {
    const {key, value} = await request.json();
    console.log(`Setting server cookies ${key}:${value}`)

    if (!key || !value) {
        return new Response(JSON.stringify({success: false, message: "No code provided"}), {
            status: 400
        });
    }

    // Set the cookie
    return new Response(JSON.stringify({success: true}), {
        status: 200,
        headers: {
            'Set-Cookie': `${key}=${encodeURIComponent(value)}; Path=/; HttpOnly;`
        },
    });
}
