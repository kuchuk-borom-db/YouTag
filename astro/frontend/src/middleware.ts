// src/middleware.ts
import type {MiddlewareHandler} from 'astro';
import {getUserDetails} from "./services/UserService.ts";


export const onRequest: MiddlewareHandler = async (context, next) => {
    // List of public routes that don't require authentication
    const publicRoutes = [
        '/login',
        '/redirect',
    ];

    // Check if the current route is a public route. Fix this
    const isPublicRoute = publicRoutes.some(route =>
        context.url.pathname === route
    );

    if(isPublicRoute){
        return next();
    }

    //Check if token is present as server side cookie
    const token = context.cookies.get("token")?.value;
    if (!token) {
        console.log("Token missing from server cookie")
        return context.redirect("/login", 302);
    }
    //Check if we can get user info from the token
    const userinfo = await getUserDetails(token);
    if (!userinfo) {
        console.log("User info null from token")
        return context.redirect("/login", 302);
    }

    //Save the user as server cookie
    context.cookies.set("user-info", userinfo, {maxAge: 60 * 60, httpOnly: true})
    return next();
};