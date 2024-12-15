import { Module } from '@nestjs/common';
import { AuthCommander } from './api/Services';
import { AuthCommanderImpl } from './internal/application/AuthCommanderImpl';

@Module({
  providers: [
    {
      provide: AuthCommander,
      useClass: AuthCommanderImpl,
    },
  ],
  exports: [AuthCommander],
})
export class CommanderModule {}
