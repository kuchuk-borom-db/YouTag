
import {defineConfig} from 'astro/config';

import tailwind from "@astrojs/tailwind";

import react from "@astrojs/react";

import vercel from "@astrojs/vercel";

export default defineConfig({
    output: "server",

    integrations: [tailwind(), react()],

    adapter: vercel({
        isr : false, //causes outdated cache data to be presented
        edgeMiddleware : false, //was causing the auth middleware to not get triggered
        maxDuration : 60,
    }),
});