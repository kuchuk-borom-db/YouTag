Quick command to build the image and push it

docker build -t youtag_nestjs .


docker tag youtag_nestjs kuchukboromdebbarma/youtag_nestjs:latest

docker push kuchukboromdebbarma/youtag_nestjs:latest


docker run -d ^
-e NOTE="Use these values are for testing only. They will be invalidated, don't worry" ^
-e OAUTH_GOOGLE_ID=604887776464-ll6h84c7ctu89i694e8jrurf6jv8dmb7.apps.googleusercontent.com ^
-e OAUTH_GOOGLE_SECRET=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 ^
-e OAUTH_GOOGLE_REDIRECT_URI=http://localhost:4321/redirect ^
-e OAUTH_GOOGLE_SCOPES=email,profile ^
-e JWT_SECRET=thisIsABIGASSSecret ^
-e JWT_EXPIRY=1y ^
-e DB_HOST=ep-orange-surf-a1pq1ti6.ap-southeast-1.aws.neon.tech ^
-e DB_USERNAME=youtag ^
-e DB_PWD=KzdI3qbx1sAB ^
-e DB_NAME=youtag ^
-e CORS_ORIGINS=http://localhost:3000,http://localhost:4321 ^
-p 4321:4321 ^
kuchukboromdebbarma/youtag_nestjs:latest






Accounts used = kuchukboromdebbarma@outlook.com or else github

Used technologies, tool and services = NestJS, GraphQL, Apollo, Astro, Java Spring Boot, Postgresql, Render, Vercel, Google Cloud Platform, OAuth2,
