import { OAUTH_PROVIDER } from '../../graphql';
import { UserDTO } from '../../user/api/DTOs';

/**
 * Operations related to User and auth
 */
export abstract class AuthCommander {
  abstract getOAuthLoginURL(
    provider: OAUTH_PROVIDER,
  ): Promise<string | null> | string;

  abstract exchangeOAuthToken(
    token: string,
    provider: OAUTH_PROVIDER,
  ): Promise<string | null>;

  abstract validateAccessToken(token: string): Promise<UserDTO | null>;
}

export abstract class TagCommander {}

export abstract class VideoCommander {}
