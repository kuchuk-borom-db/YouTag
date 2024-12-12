import { OAuthUserDTO } from './DTOs';

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
