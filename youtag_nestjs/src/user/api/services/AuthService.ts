import OAuthUserDTO from '../dtos/OAuthUser.dto';

export default abstract class AuthService {
  abstract getOAuthLoginURL(): Promise<string | null> | string | null;

  abstract getOAuthUserInfo(
    code: string,
  ): Promise<OAuthUserDTO | null> | OAuthUserDTO | null;
}
