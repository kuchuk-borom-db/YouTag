import { Module } from '@nestjs/common';
import { AuthCommander, TagCommander, VideoCommander } from './api/Services';
import { AuthCommanderImpl } from './internal/application/AuthCommanderImpl';
import { UserModule } from '../user/user.module';
import { TagCommanderImpl } from './internal/application/TagCommanderImpl';
import { VideoCommanderImpl } from './internal/application/VideoCommanderImpl';

@Module({
  exports: [AuthCommander, TagCommander, VideoCommander],
  imports: [UserModule],
  providers: [
    {
      provide: AuthCommander,
      useClass: AuthCommanderImpl,
    },

    {
      provide: TagCommander,
      useClass: TagCommanderImpl,
    },
    {
      provide: VideoCommander,
      useClass: VideoCommanderImpl,
    },
  ],
})
export class CommanderModule {}
