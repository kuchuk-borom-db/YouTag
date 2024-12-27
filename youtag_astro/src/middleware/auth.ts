import {defineMiddleware} from "astro/middleware";

const logLabel = "Middleware-Auth";

export const auth = defineMiddleware(async (context, next) => {
    console.log(`[${logLabel}] Path being checked: ${context.url.pathname}`);
    const pathName = context.url.pathname;
    console.log(`[${logLabel}] Current page = ${pathName}`);
    console.log(`[${logLabel}] Current full path = ${context.url}`);
    console.log(`[${logLabel}] Auth middleware triggered on path name ${pathName}`);

    if (!isAuthPath(pathName)) {
        console.log(`[${logLabel}] Hit a public route: ${pathName}`);
        return next();
    }

    try {
        console.log(`[${logLabel}] Cookies: ${JSON.stringify(context.cookies)}`);
        const token = context.cookies.get("token")?.value;
        console.log(`[${logLabel}] Cookies: ${JSON.stringify(context.cookies)}`);
        if (!token) {
            console.log(`[${logLabel}] No token provided. Redirecting to login page.`);
            const response = new Response(null, {
                status: 302,
                headers: {
                    Location: "/login",
                },
            });
            console.log(`[${logLabel}] Redirecting to ${response.url}`);
            return response;
        }

        // Further token validation logic can go here, if needed.

        return next();
    } catch (error) {
        console.error(`[${logLabel}] Error occurred: ${error}`);
        console.log(`[${logLabel}] Redirecting to login page due to an error.`);
        const response = new Response(null, {
            status: 302,
            headers: {
                Location: "/login",
            },
        });
        console.log(`[${logLabel}] Redirecting to ${response.url}`);
        return response;
    }
});

function isAuthPath(pathName: string): boolean {
    const publicPaths = [
        "/login",
        "/redirect",
        "/api/cookie",
        "/api/delete-account"
    ];
    // Check if the path is a public route
    return !publicPaths.includes(pathName);
}
