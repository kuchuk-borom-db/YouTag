// @ts-check
// @ts-check
import {defineConfig} from 'astro/config';

import tailwind from "@astrojs/tailwind";

import react from "@astrojs/react";

import vercel from "@astrojs/vercel";

export default defineConfig({
    //Prerender every page as static site by default
    output: "server",

    integrations: [tailwind(), react()],

    experimental: {
    },

    adapter: vercel({
        webAnalytics: {
            enabled: true,
        },
        isr : true,

        edgeMiddleware: true,
        maxDuration: 8,
    }),
});