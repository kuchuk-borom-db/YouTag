import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { EnvironmentConst } from '../../../Utils/Constants';
import { v4 } from 'uuid';
import { AuthService } from '../../api/Services';
import * as simpleOAuth2 from 'simple-oauth2';
import { OAuthUserDTO } from '../../api/DTOs';

@Injectable()
export class GoogleAuthServiceImpl implements AuthService {
  private readonly logger: Logger;
  private oauth2: simpleOAuth2.AuthorizationCode;

  constructor(private env: ConfigService) {
    this.logger = new Logger(GoogleAuthServiceImpl.name);
    this.oauth2 = new simpleOAuth2.AuthorizationCode({
      client: {
        id: this.env.get<string>(EnvironmentConst.OAuth.Google.ClientID),
        secret: this.env.get<string>(
          EnvironmentConst.OAuth.Google.ClientSecret,
        ),
      },
      auth: {
        tokenHost: 'https://accounts.google.com',
        authorizePath: '/o/oauth2/auth',
        tokenPath: '/o/oauth2/token',
      },
    });
  }

  getOAuthLoginURL(): string | null {
    this.logger.debug('Generating Google OAuth login URL');
    const scopes = this.env
      .get<string>(EnvironmentConst.OAuth.Google.Scopes)
      .split(',');
    const state = v4(); // Generate a state parameter for CSRF protection
    const url = this.oauth2.authorizeURL({
      redirect_uri: this.env.get<string>(
        EnvironmentConst.OAuth.Google.RedirectURI,
      ),
      scope: scopes,
      state: state,
    });
    this.logger.debug(`Generated Google OAuth Login URL: ${url}`);
    return url;
  }

  async getOAuthUserInfo(code: string): Promise<OAuthUserDTO | null> {
    try {
      const tokenParams = {
        code,
        redirect_uri: this.env.get<string>(
          EnvironmentConst.OAuth.Google.RedirectURI,
        ),
      };

      // Get the access token using the authorization code
      const accessToken = await this.oauth2.getToken(tokenParams);

      const accessTokenString = accessToken.token.access_token as string;

      // Fetch user info from Google API
      const response = await fetch(
        'https://www.googleapis.com/oauth2/v3/userinfo',
        {
          headers: { Authorization: `Bearer ${accessTokenString}` },
        },
      );

      const userInfo = await response.json();
      return {
        id: userInfo.email, // Set user ID as email
        name: userInfo.name,
        thumbnailUrl: userInfo.picture ?? null, // Return thumbnail URL if available
      };
    } catch (error) {
      this.logger.error(
        `Failed to get user info from Google: ${error.message}`,
      );
      return null;
    }
  }
}
