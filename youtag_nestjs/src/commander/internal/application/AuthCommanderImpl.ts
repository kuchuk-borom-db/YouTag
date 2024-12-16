import { OAuthProvider } from 'src/user/api/Enums';
import { AuthCommander } from '../../api/Services';
import { OAUTH_PROVIDER } from '../../../graphql';
import {
  AuthJwtService,
  AuthService,
  UserService,
} from '../../../user/api/Services';
import { Injectable, Logger } from '@nestjs/common';
import { UserDTO } from 'src/user/api/DTOs';

@Injectable()
export class AuthCommanderImpl extends AuthCommander {
  constructor(
    private readonly authService: AuthService,
    private readonly userService: UserService,
    private readonly jwtService: AuthJwtService,
  ) {
    super();
  }

  private readonly log = new Logger(AuthCommanderImpl.name);

  getOAuthLoginURL(provider: OAUTH_PROVIDER): Promise<string | null> | string {
    return this.authService.getOAuthLoginURL();
  }

  exchangeOAuthToken(token: string, provider: OAuthProvider): Promise<string> {
    throw new Error('Method not implemented.');
  }

  async validateAccessToken(token: string): Promise<UserDTO | null> {
    try {
      const userId = this.jwtService.verifyJwtAccessToken(token);
      return await this.userService.getUserById(userId);
    } catch (error) {
      this.log.error(`Error at validate access token ${error}`);
      return null;
    }
  }
}
