import { Column, Entity, PrimaryColumn } from 'typeorm';

@Entity({ name: 'videos', schema: 'youtag' })
export class VideoEntity {
  @PrimaryColumn({ type: 'varchar' })
  id: string;
  @Column({ nullable: false, type: 'varchar' })
  title: string;
  @Column({ type: 'varchar', nullable: false })
  author: string;
  @Column({ name: 'author_url', type: 'varchar', nullable: false })
  authorUrl: string;
  @Column({ name: 'thumbnail_url', type: 'varchar', nullable: false })
  thumbnailUrl: string;
}
