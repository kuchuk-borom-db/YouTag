<img src="assets/youtag_logo-no-bg.ico">

**Organize and Access Your Favorite YouTube Videos with Ease**

Imagine these scenarios:

🎵 **Music Lover's Challenge**  
You've saved hundreds of music videos on YouTube - Taylor Swift's pop hits, classic rock from Queen, and acoustic covers of your favorite songs. Now you want to find all of Taylor's pop ballads, but they're scattered across different playlists. Without proper organization, you'd spend time scrolling through each playlist trying to find them.

💻 **Developer's Learning Path**  
As a programmer, you've bookmarked numerous tutorial videos - Java basics, Python projects, system design interviews, and coding challenge solutions. When preparing for a system design interview, you want to quickly access all relevant videos without digging through your entire watch history.

This is where YouTag comes in. Instead of endless searching, you can:
- Tag Taylor's videos with "pop", "Taylor_Swift", "ballad"
- Tag programming videos with "java", "system_design", "interview_prep"

Then simply search using these tags to instantly find exactly what you're looking for!

YouTag is a personal video library tool designed to help you save, tag, and search YouTube videos effortlessly. By adding custom tags to each video, you can create a personalized catalog that simplifies finding the videos you want when you need them.

<a href="https://you-tag.vercel.app/">
Visit (Please keep in mind that if server is shut down due to inactivity, give it around a minute to start back up.)
</a>

## Key Features

1. **Easy Sign-In with Google**  
   Quick and secure login using your Google account, so you can start organizing your videos instantly.

2. **Save Videos with Links**  
   Just paste a YouTube link to save a video to your library, making it quick and hassle-free.

3. **Tag and Organize Videos**  
   Add custom tags to each saved video, allowing you to categorize and label them according to your preferences. This makes searching for specific videos a breeze, even in a large collection.

## Benefits

- **Effortless Video Management**  
  Say goodbye to endless scrolling. YouTag's tagging system lets you sort and find your videos easily, tailored to your own organization style.

- **Personalized Organization**  
  Your tags are unique to you, providing a flexible way to categorize videos for study, work, entertainment, and more.

- **Quick Access Anytime**  
  With YouTag, your favorite videos are just a search away, thanks to a fully customized tagging system.


## Technical Documentation

### Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Backend Design](#backend-design)
- [Authentication & Authorization](#authentication--authorization)
- [Frontend Design](#frontend-design)

### Overview
YouTags is a platform that allows users to organize their YouTube videos using custom userTags. Users can authenticate with their Google account, add YouTube videos to their collection, and manage them with personalized userTags for better organization and searchability.

### Architecture
The application follows a modular monolith architecture with clear boundaries between modules. A special orchestrator module handles cross-module operations to ensure simplicity and ease of debugging. This approach prioritizes code clarity and maintainability while minimizing coupling between services.

### Technology Stack
#### Frontend
- **Framework**: Astro with ReactJS
- **Styling**: TailwindCSS
- **Hosting**: Vercel

#### Backend
- **Frameworks**:
   - NestJS with GraphQL (serverless deployment)
   - Spring Boot (optional for non-serverless scenarios)
- **Core Technologies**:
   - GraphQL: Flexible querying and reduced over-fetching
   - Service-Level Caching: Optimized performance
   - Event Dispatching: Used for fire-and-forget operations like cleaning unused videos
   - Docker: Containerization for deployments
- **Hosting**: Render
- **Database**: PostgreSQL hosted on Neon

### Backend Design
The backend is organized into independent modules to promote clean separation of concerns.

#### Module Structure
Each module contains the following components:
- **API**: Public interfaces and operations exposed to other modules
- **Internals**: Business logic and utilities exclusive to the module

Modules are designed to be pure and do not interact with other modules directly. Cross-module interactions are handled by the orchestrator module.

#### Orchestrator Module
The orchestrator module coordinates operations that involve multiple modules. While this centralization introduces some tight coupling, it simplifies debugging and refactoring by consolidating cross-module logic.

#### GraphQL
- Flexible querying minimizes under-fetching and over-fetching.
- Avoids GraphQL's N+1 problem with efficient service-level caching. Dataloaders are not used in this project (atleast for now)

#### Event Handling
- Events are used selectively for fire-and-forget operations.
- Example: Cleaning up unused videos after user actions.

#### Caching Strategy
- Service-level caching optimizes performance.
- A centralized invalidate-cache mechanism ensures consistency across modules.
- Example: Clearing tag-related cache when a video is removed.

### Authentication & Authorization
- **Login**: Google OAuth2 for user authentication and fetching user information.
- **Session Management**: JWT tokens with user ID as the subject.
- **Request Handling**: Clients include JWT tokens as Bearer tokens in authorization headers.

#### Security Configuration
- Stateless API with no session management.
- CORS setup for development (all origins allowed) and production (strict policies).

#### Authentication Flow
1. User logs in via Google OAuth.
2. Server validates the OAuth response.
3. JWT token is generated upon request.
4. Subsequent requests authenticate via the JWT token.

### Frontend Design
The frontend is designed for modularity and efficient server communication.

#### Structure
- **Components**: Reusable UI elements
- **Pages**: Represent application views
- **API**: Handles server communication and SSR logic
- **Middleware**: Includes logic for redirection and authentication checks

#### Features
- Hosted on Vercel for seamless deployment.
- TailwindCSS for rapid styling and consistent design.


