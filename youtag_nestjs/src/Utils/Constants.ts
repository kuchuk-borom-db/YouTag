export namespace EnvironmentConst {
    export namespace OAuth {
        export namespace Google {
            export const ClientID = 'OAUTH_GOOGLE_ID';
            export const ClientSecret = 'OAUTH_GOOGLE_SECRET';
            export const RedirectURI = 'OAUTH_GOOGLE_REDIRECT_URI';
            export const Scopes = 'OAUTH_GOOGLE_SCOPES';
        }
    }

    export namespace JWT {
        export const Secret = 'JWT_SECRET';
        export const Expiry = 'JWT_EXPIRY';
    }

    export namespace Db {
        export const Host = 'DB_HOST';
        export const Username = 'DB_USERNAME';
        export const Password = 'DB_PWD';
        export const DatabaseName = 'DB_NAME';
    }
}
export namespace Events {
    export const REMOVE_UNUSED_VIDEOS = "remove_unused_videos"
    export const ADDED_TAGS_TO_VIDEOS = "added_tags_to_videos";
}