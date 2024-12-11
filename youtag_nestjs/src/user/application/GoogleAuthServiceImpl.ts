import OAuthUserDTO from '../api/dtos/OAuthUser.dto';
import AuthService from '../api/services/AuthService';
import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { EnvironmentConst } from '../../Utils/Constants';
import * as simpleOAuth2 from "simple-oauth2";
import { v4 } from 'uuid';

@Injectable()
export default class GoogleAuthServiceImpl implements AuthService {
  private readonly logger: Logger;
  private oauth2: simpleOAuth2.AuthorizationCode;

  constructor(private env: ConfigService) {
    this.logger = new Logger(GoogleAuthServiceImpl.name);
  }

  getOAuthLoginURL(): Promise<string | null> {
    this.logger.debug('Generating Google OAuth login URL');
    const scopes = this.env
      .get<string>(EnvironmentConst.OAuth.Google.Scopes)
      .split(',');
    const state = v4();
    const url = this.oauth2.authorizeURL({
      redirect_uri: this.env.get<string>(
        EnvironmentConst.OAuth.Google.RedirectURI,
      ),
      scope: scopes,
      state: state,
    });
    this.logger.debug(`Generated Google OAuth Login URL : ${url}`);
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

      // Extract the access token from the response
      const accessTokenString: string = accessToken.token
        .access_token as string;

      const response = await fetch(
        'https://www.googleapis.com/oauth2/v3/userinfo',
        {
          headers: {
            Authorization: `Bearer ${accessTokenString}`,
          },
        },
      );

      const userInfo = await response.json();
      return {
        id: userInfo.email,
        name: userInfo.name,
        thumbnailUrl: userInfo.picture ? userInfo.picture : null,
      };
    } catch (error) {
      this.logger.error(
        `Failed to get user info from Google: ${error.message} : STACK \n ${error}`,
      );
      return null;
    }
  }
}
