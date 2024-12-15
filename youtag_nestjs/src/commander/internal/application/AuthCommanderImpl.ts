import { OAuthProvider } from 'src/user/api/Enums';
import { AuthCommander } from '../../api/Services';

export class AuthCommanderImpl extends AuthCommander {
  getOAuthLoginURL(provider: OAuthProvider): Promise<string> {
    throw new Error('Method not implemented.');
  }

  exchangeOAuthToken(token: string, provider: OAuthProvider): Promise<string> {
    throw new Error('Method not implemented.');
  }
}
