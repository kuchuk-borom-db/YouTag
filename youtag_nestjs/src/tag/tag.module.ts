import { Module } from '@nestjs/common';
import { TagEntity } from './internal/domain/Entities';
import { TypeOrmModule } from '@nestjs/typeorm';
import { TagService } from './api/Services';
import TagServiceImpl from './internal/application/TagServiceImpl';
import { CacheModule } from '@nestjs/cache-manager';

@Module({
  imports: [
    TypeOrmModule.forFeature([TagEntity]),
    CacheModule.register({
      ttl: 240,
    }),
  ],
  providers: [
    {
      provide: TagService,
      useClass: TagServiceImpl,
    },
  ],
  exports: [TagService],
})
export class TagModule {}
