import { OAuthProvider } from '../../user/api/Enums';

/**
 * Operations related to User and auth
 */
export abstract class AuthCommander {
  abstract getOAuthLoginURL(provider: OAuthProvider): Promise<string>;

  abstract exchangeOAuthToken(
    token: string,
    provider: OAuthProvider,
  ): Promise<string>;
}
