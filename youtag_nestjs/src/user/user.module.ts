import { Module } from '@nestjs/common';

import { JwtModule } from '@nestjs/jwt';
import { AuthJwtService, AuthService, UserService } from './api/Services';
import { GoogleAuthServiceImpl } from './internal/application/GoogleOAuthService';
import { AuthJwtServiceImpl } from './internal/application/AuthJWTServiceImpl';
import { TypeOrmModule } from '@nestjs/typeorm';
import { UserEntity } from './internal/domain/Entities';
import UserServiceImpl from './internal/application/UserServiceImpl';

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
    {
      provide: UserService,
      useClass: UserServiceImpl,
    },
  ],
  imports: [JwtModule, TypeOrmModule.forFeature([UserEntity])],
  exports: [AuthJwtService, AuthService, UserService],
})
export class UserModule {}
