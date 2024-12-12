import { Column, Entity, PrimaryColumn } from 'typeorm';

@Entity({ name: 'videos', schema: 'youtag' })
export class VideoEntity {
  @PrimaryColumn({ type: 'varchar' })
  id: string;
  @Column({ nullable: true, type: 'varchar' })
  title?: string;
  @Column({ type: 'varchar', nullable: true })
  description?: string;
  @Column({ name: 'thumbnail_url', type: 'varchar', nullable: true })
  thumbnailUrl: string;
}
