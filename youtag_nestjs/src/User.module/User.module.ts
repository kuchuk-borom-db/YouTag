import { Injectable, Logger, Module } from '@nestjs/common';
import { JwtModule, JwtService } from '@nestjs/jwt';
import { ConfigService } from '@nestjs/config';
import { EnvironmentConst } from '../Utils/Constants';
import * as simpleOAuth2 from 'simple-oauth2';
import { v4 } from 'uuid';

export namespace API {
  export enum OAuthProvider {
    GOOGLE = 'GOOGLE',
  }

  export type OAuthUserDTO = {
    name: string;
    id: string; // Typically, this should be email from Google.
    thumbnailUrl?: string;
  };

  export abstract class AuthService {
    /**
     * @return {string} Authorization URL
     * @return {null} failed to get Login URL
     */
    abstract getOAuthLoginURL(): Promise<string | null> | string | null;

    /**
     * Exchange code for user info
     * @param code code to exchange userinfo with
     * @return {OAuthUserDTO} returns user info or null if failed
     */
    abstract getOAuthUserInfo(
      code: string,
    ): Promise<OAuthUserDTO | null> | OAuthUserDTO | null;
  }

  export abstract class AuthJwtService {
    /**
     * Generate JWT access token with userId as subject
     * @return {string} Generated Token
     */
    abstract generateJwtAccessToken(userId: string): string | null;

    /**
     * Verify the signature and check expiry date of the token
     * @param token token to verify
     * @return {string} userId extracted from subject
     */
    abstract verifyJwtAccessToken(token: string): string | null;
  }
}

namespace Application {
  import AuthJwtService = API.AuthJwtService;
  import AuthService = API.AuthService;
  import OAuthUserDTO = API.OAuthUserDTO;

  @Injectable()
  export class AuthJwtServiceImpl implements AuthJwtService {
    private readonly log: Logger;

    constructor(
      private jwtService: JwtService,
      private env: ConfigService,
    ) {
      this.log = new Logger(AuthJwtServiceImpl.name);
    }

    generateJwtAccessToken(userId: string): string | null {
      try {
        this.log.debug(`Generating Access token for user ${userId}`);
        const token = this.jwtService.sign(
          {
            subject: userId,
          },
          {
            secret: this.env.get<string>(EnvironmentConst.JWT.Secret),
            algorithm: 'HS256',
            issuer: 'YouTag',
            expiresIn: this.env.get<string>(EnvironmentConst.JWT.Expiry),
          },
        );
        this.log.debug(`Generated Access token for user ${userId}`);
        return token;
      } catch (err) {
        this.log.error(`Error generating JWT token for user ${userId}: ${err}`);
        return null;
      }
    }

    verifyJwtAccessToken(token: string): string | null {
      try {
        this.log.debug('Verifying JWT access token');
        const decoded = this.jwtService.verify(token, {
          secret: this.env.get<string>(EnvironmentConst.JWT.Secret),
          algorithms: ['HS256'],
        });
        this.log.debug(
          `JWT token verified successfully for user ${decoded.sub}`,
        );
        return decoded.sub; // Return userId from the subject
      } catch (error) {
        this.log.error(`JWT token verification failed: ${error.message}`);
        return null;
      }
    }
  }

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
}

export namespace UserModule {
  import AuthJwtServiceImpl = Application.AuthJwtServiceImpl;
  import GoogleAuthServiceImpl = Application.GoogleAuthServiceImpl;
  import AuthJwtService = API.AuthJwtService;
  import AuthService = API.AuthService;

  @Module({
    providers: [
      {
        provide: AuthService,
        useClass: GoogleAuthServiceImpl,
      },
      {
        provide: AuthJwtService,
        useClass: AuthJwtServiceImpl,
      },
    ],
    imports: [JwtModule],
    exports: [AuthJwtService, AuthService],
  })
  export class UserModule {}
}
