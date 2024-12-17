import { Module } from '@nestjs/common';
import { VideoService } from './api/Services';
import VideoServiceImpl from './internal/application/VideoServiceImpl';
import { TypeOrmModule } from '@nestjs/typeorm';
import { VideoEntity } from './internal/domain/Entities';
import { CacheModule } from '@nestjs/cache-manager';

@Module({
  imports: [
    TypeOrmModule.forFeature([VideoEntity]),
    CacheModule.register({
      ttl: 120,
    }),
  ],
  providers: [
    {
      provide: VideoService,
      useClass: VideoServiceImpl,
    },
  ],
  exports: [VideoService],
})
export class VideoModule {}
