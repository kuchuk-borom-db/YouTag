import { Column, Entity, PrimaryColumn } from 'typeorm';

@Entity({ name: 'tags', schema: 'youtag' })
export class TagEntity {
  @PrimaryColumn({ name: 'user_id', type: 'varchar', nullable: false })
  userId: string;
  @PrimaryColumn({ name: 'video_id', type: 'varchar', nullable: false })
  videoId: string;
  @Column({ type: 'varchar', nullable: false })
  tag: string;
}
