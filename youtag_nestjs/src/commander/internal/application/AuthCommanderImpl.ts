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

  async exchangeOAuthToken(
    token: string,
    provider: OAUTH_PROVIDER,
  ): Promise<string | null> {
    try {
      const userInfo = await this.authService.getOAuthUserInfo(token);
      if (!userInfo || !userInfo.id) {
        return null;
      }
      //Create user in database
      const result = await this.userService.createUser({
        id: userInfo.id,
        name: userInfo.name,
        thumbnailUrl: userInfo.thumbnailUrl,
      });
      if (!result) {
        this.log.warn('Failed to create user in database');
      }
      return this.jwtService.generateJwtAccessToken(userInfo.id);
    } catch (err) {
      this.log.error(`Error at exchangeOAuthToken ${err}`);
      return null;
    }
  }

  async validateAccessToken(token: string): Promise<UserDTO | null> {
    try {
      const userId = this.jwtService.verifyJwtAccessToken(token);
      if (!userId) {
        this.log.error(`User undefined in token`);
        return null;
      }
      const user = await this.userService.getUserById(userId);
      this.log.debug(`Found user ${JSON.stringify(user)}`);
      return user;
    } catch (error) {
      this.log.error(`Error at validate access token ${error}`);
      return null;
    }
  }
}
