import { defineMiddleware } from "astro/middleware";

const logLabel = "Middleware-Auth";

export const auth = defineMiddleware(async (context, next) => {
    const pathName = context.url.pathname;
    console.log(`Current page = ${pathName}`);
    console.log(`Auth middleware triggered on path name ${pathName}`);

    // If it's a public path, continue
    if (!isAuthPath(pathName)) {
        console.log(`Hit a public route ${pathName}`);
        return next();
    }

    const token = context.cookies.get("token")?.value;

    // If no token, redirect to absolute login URL
    if (token == undefined) {
        console.log("No token provided. Redirecting to login page");

        // Use an absolute URL for consistent redirection across environments
        return context.redirect(new URL("/login", context.url.origin).toString());
    }

    return next();
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