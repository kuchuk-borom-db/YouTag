import { Column, Entity, PrimaryColumn } from 'typeorm';

@Entity({ name: 'users', schema: 'youtag' })
export class UserEntity {
  @Column({ name: 'name' })
  name: string;
  @PrimaryColumn({ name: 'id' })
  userId: string;
  @Column({ name: 'thumbnail_url' })
  thumbnail: string;
  @Column({ name: 'last_active' })
  lastActive: Date;
}
