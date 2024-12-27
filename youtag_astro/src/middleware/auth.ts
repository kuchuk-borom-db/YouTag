import { defineMiddleware } from "astro/middleware";

const logLabel = "Middleware-Auth"
export const auth = defineMiddleware(async (context, next) => {
    const pathName = context.url.pathname;
    console.log(`Current page = ${pathName}`)
    console.log(`Current full path = ${context.url}`)

    console.log(`Auth middleware triggered on path name ${pathName}`)
    if (!isAuthPath(pathName)) {
        console.log(`Hit a public route ${pathName}`);
        return next();
    }

    console.log(`Cookies : ${JSON.stringify(context.cookies)}`)
    const token = context.cookies.get("token")?.value
    if (token == undefined) {
        console.log(`No token provided. Redirecting to login page`)
        // Add the current URL as a redirect parameter
        const returnUrl = encodeURIComponent(context.url.pathname + context.url.search)
        const response = context.redirect(`/login?redirect=${returnUrl}`)
        console.log(`Redirecting to ${response.url}`)
        return response;
    }
    return next()
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