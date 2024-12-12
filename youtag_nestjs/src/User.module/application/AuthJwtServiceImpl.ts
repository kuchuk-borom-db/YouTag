import { AuthJwtService } from '../api/Services';
import { Injectable, Logger } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { EnvironmentConst } from '../../Utils/Constants';
import { ConfigService } from '@nestjs/config';

@Injectable()
export default class AuthJwtServiceImpl implements AuthJwtService {
  private readonly log: Logger;

  constructor(
    private jwtService: JwtService,
    private env: ConfigService,
  ) {
    this.log = new Logger((typeof AuthJwtServiceImpl).toString());
  }

  generateJwtAccessToken(userId: string): string | null {
    try {
      this.log.debug(`Generating Access token for user ${userId}`);
      const token = this.jwtService.sign({
        secret: this.env.get(EnvironmentConst.JWT.Secret),
        algorithm: 'HS256',
        subject: userId,
        issuer: 'YouTag',
        expiresIn: this.env.get(EnvironmentConst.JWT.Expiry),
      });
      this.log.debug(`Generated Access token for user ${userId} is ${token} `);
      return token;
    } catch (err) {
      this.log.error(`Get Google Login url error ${err}`);
      return null;
    }
  }

  verifyJwtAccessToken(token: string): string | null {
    try {
      this.log.debug(`Verifying JWT access token`);
      const decoded = this.jwtService.verify(token, {
        secret: this.env.get(EnvironmentConst.JWT.Secret),
        algorithms: ['HS256'],
      });

      this.log.debug(`JWT token verified successfully for user ${decoded.sub}`);
      return decoded;
    } catch (error) {
      this.log.error(
        `JWT token verification failed: ${error.message} STACK \n ${error}`,
      );
      return null;
    }
  }
}
