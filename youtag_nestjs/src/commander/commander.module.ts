import { Module } from '@nestjs/common';
import { AuthCommander } from './api/Services';
import { AuthCommanderImpl } from './internal/application/AuthCommanderImpl';
import { UserModule } from '../user/user.module';

@Module({
  providers: [
    {
      provide: AuthCommander,
      useClass: AuthCommanderImpl,
    },
  ],
  exports: [AuthCommander],
  imports: [UserModule],
})
export class CommanderModule {}
