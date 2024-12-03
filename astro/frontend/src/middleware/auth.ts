import {defineMiddleware} from "astro/middleware";
import {getUserInfo} from "../services/AuthService.ts";

/**
 * Check if server-cookie contains token.
 */
const logLabel = "Middleware-Auth"
export const auth = defineMiddleware(async (context, next) => {
    const pathName = context.url.pathname;
    console.log(`Auth middleware triggered on path name ${pathName}`)
    if (!isAuthPath(pathName)) {
        console.log(`Hit a public route ${pathName}`);
        return next();
    }

    const token = context.cookies.get("token")?.value
    if (token == undefined) {
        console.log("No token provided. Redirecting to login page")
        return context.redirect('/login');
    }
    //Get user info from the token
    const userInfo = await getUserInfo(token);
    if (userInfo == null) {
        console.log("User info was invalid using the token. Redirecting to login page")
        return context.redirect('/login');
    }
    //Set user as server-cookie
    context.cookies.set("user-info", userInfo);
    return next()
});

function isAuthPath(pathName: string): boolean {
    const publicPaths = [
        "/login",
        "/redirect",
        "/api/cookie"
    ];
    // Check if the path is a public route
    return !publicPaths.includes(pathName);
}

