
import {defineConfig} from 'astro/config';

import tailwind from "@astrojs/tailwind";

import react from "@astrojs/react";

import vercel from "@astrojs/vercel";

export default defineConfig({
    output: "server",

    integrations: [tailwind(), react()],

    adapter: vercel({
        isr : true,
        edgeMiddleware : true,
        maxDuration : 60,
    }),
});