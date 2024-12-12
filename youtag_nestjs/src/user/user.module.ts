import { Module } from '@nestjs/common';

import { JwtModule } from '@nestjs/jwt';
import { AuthJwtService, AuthService } from './api/Services';
import { GoogleAuthServiceImpl } from './internal/application/GoogleOAuthService';
import { AuthJwtServiceImpl } from './internal/application/AuthJWTServiceImpl';

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
