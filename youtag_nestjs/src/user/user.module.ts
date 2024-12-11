import { Module } from '@nestjs/common';
import GoogleAuthServiceImpl from './application/GoogleAuthServiceImpl';
import AuthService from './api/services/AuthService';

@Module({
  providers: [
    {
      provide: AuthService,
      useClass: GoogleAuthServiceImpl,
    },
  ],

  exports: [AuthService],
})
export class UserModule {}
