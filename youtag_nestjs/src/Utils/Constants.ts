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
    export const Expiry = '30d';
  }
}
