import { Module } from '@nestjs/common';
import { VideoService } from './api/Services';
import VideoServiceImpl from './internal/application/VideoServiceImpl';
import { TypeOrmModule } from '@nestjs/typeorm';
import { VideoEntity } from './internal/domain/Entities';

@Module({
  imports: [TypeOrmModule.forFeature([VideoEntity])],
  providers: [
    {
      provide: VideoService,
      useClass: VideoServiceImpl,
    },
  ],
  exports: [VideoService],
})
export class VideoModule {}
