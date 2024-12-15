import { Module } from '@nestjs/common';
import { TagEntity } from './internal/domain/Entities';
import { TypeOrmModule } from '@nestjs/typeorm';
import { TagService } from './api/Services';
import TagServiceImpl from './internal/application/TagServiceImpl';

@Module({
  imports: [TypeOrmModule.forFeature([TagEntity])],
  providers: [
    {
      provide: TagService,
      useClass: TagServiceImpl,
    },
  ],
  exports: [TagService],
})
export class TagModule {}
