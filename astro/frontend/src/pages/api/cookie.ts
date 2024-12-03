import type {APIRoute} from "astro";

export const POST: APIRoute = async ({params, request}) => {
    console.log("cookie post request");
    const body = await request.json();
    const key = body['key'];
    const value = body['value'];

    if (!key || !value) {
        console.error("invalid key and/or value");
        return new Response(JSON.stringify({error: "invalid key and/value"}));
    }
}