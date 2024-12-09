import type {APIRoute} from "astro";

export const POST: APIRoute = async (context) => {
    console.log("cookie post request");

    const req = context.request;
    let body;
    try {
        body = await req.json();
    } catch (error) {
        console.error("Failed to parse request body:", error);
        return new Response(
            JSON.stringify({error: "Invalid JSON body"}),
            {
                status: 400,
                headers: {"Content-Type": "application/json"},
            }
        );
    }

    const key = body['name'];
    const value = body['value'];
    const httpOnly: boolean = body['httpOnly'];

    if (!key || !value) {
        console.error("Invalid key and/or value");
        return new Response(
            JSON.stringify({error: "Invalid key and/or value"}),
            {
                status: 400,
                headers: {"Content-Type": "application/json"},
            }
        );
    }

    // Add Set-Cookie header
    context.cookies.set(key, value, {
        httpOnly: httpOnly,
        path: "/"
    });
    //This will set the cookie in server side
    return new Response("Cookie set", {
        status: 200,
    });
};

export const DELETE: APIRoute = async (context) => {
    console.log("cookie Delete request");
    const req = context.request;
    let body;
    try {
        body = await req.json();
    } catch (error) {
        console.error("Failed to parse request body:", error);
        return new Response(
            JSON.stringify({error: "Invalid JSON body"}),
            {
                status: 400,
                headers: {"Content-Type": "application/json"},
            }
        );
    }

    const key = body['name'];
    if (!key) {
        console.error("Invalid key and/or value");
        return new Response(
            JSON.stringify({error: "Invalid key and/or value"}),
            {
                status: 400,
                headers: {"Content-Type": "application/json"},
            }
        );
    }

    context.cookies.delete(key, {
        httpOnly: false,
        path: "/"
    });
    //This will set the cookie in server side
    return new Response("Cookie set", {
        status: 200,
    });
}
export const prerender = false;