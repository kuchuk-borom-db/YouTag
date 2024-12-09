import {defineMiddleware} from "astro/middleware";

/**
 * Check if server-cookie contains token.
 */
const logLabel = "Middleware-Auth"
export const auth = defineMiddleware(async (context, next) => {
    const pathName = context.url.pathname;
    console.log(`Current page = ${pathName}`)

    console.log(`Auth middleware triggered on path name ${pathName}`)
    if (!isAuthPath(pathName)) {
        console.log(`Hit a public route ${pathName}`);
        return next();
    }
    console.log(`Cookies : ${JSON.stringify(context.cookies)}`)
    const token = context.cookies.get("token")?.value
    if (token == undefined) {
        console.log("No token provided. Redirecting to login page")
        context.redirect("/login")
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