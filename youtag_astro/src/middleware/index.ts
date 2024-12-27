console.log("Middleware loaded and sequence initialized");

import {auth} from "./auth.ts"
import {sequence} from "astro/middleware";

export const onRequest = sequence(auth);