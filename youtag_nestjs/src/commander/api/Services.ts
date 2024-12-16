import { OAuthProvider } from '../../user/api/Enums';
import { OAUTH_PROVIDER } from '../../graphql';

/**
 * Operations related to User and auth
 */
export abstract class AuthCommander {
  abstract getOAuthLoginURL(
    provider: OAUTH_PROVIDER,
  ): Promise<string | null> | string;

  abstract exchangeOAuthToken(
    token: string,
    provider: OAuthProvider,
  ): Promise<string>;
}
