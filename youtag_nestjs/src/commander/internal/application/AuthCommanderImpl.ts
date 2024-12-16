import { OAuthProvider } from 'src/user/api/Enums';
import { AuthCommander } from '../../api/Services';
import { OAUTH_PROVIDER } from '../../../graphql';
import { AuthService } from '../../../user/api/Services';
import { Injectable } from '@nestjs/common';

@Injectable()
export class AuthCommanderImpl extends AuthCommander {
  constructor(private readonly authService: AuthService) {
    super();
  }

  getOAuthLoginURL(provider: OAUTH_PROVIDER): Promise<string | null> | string {
    return this.authService.getOAuthLoginURL();
  }

  exchangeOAuthToken(token: string, provider: OAuthProvider): Promise<string> {
    throw new Error('Method not implemented.');
  }
}
