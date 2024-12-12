import { Module } from '@nestjs/common';
import GoogleAuthServiceImpl from './application/GoogleAuthServiceImpl';
import { AuthJwtService, AuthService } from './api/Services';
import AuthJwtServiceImpl from './application/AuthJwtServiceImpl';

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

  exports: [AuthService, AuthJwtService],
})
export class UserModule {}
