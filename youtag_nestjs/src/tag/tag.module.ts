import { Module } from '@nestjs/common';
import { TagEntity } from './internal/domain/Entities';
import { TypeOrmModule } from '@nestjs/typeorm';

@Module({
  imports: [TypeOrmModule.forFeature([TagEntity])],
})
export class TagModule {}
