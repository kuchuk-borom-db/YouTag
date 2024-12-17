import { Injectable, Logger } from '@nestjs/common';
import { AuthJwtService } from '../../api/Services';
import { JwtService } from '@nestjs/jwt';
import { ConfigService } from '@nestjs/config';
import { EnvironmentConst } from '../../../Utils/Constants';

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
      this.log.debug(`JWT token verified successfully for user ${decoded.subject}`);
      return decoded.subject; // Return userId from the subject
    } catch (error) {
      this.log.error(`JWT token verification failed: ${error.message}`);
      return null;
    }
  }
}
