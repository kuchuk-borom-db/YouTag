import { UserDTO } from 'src/user/api/DTOs';
import { UserService } from '../../api/Services';
import { Logger } from '@nestjs/common';
import { Repository } from 'typeorm';
import { UserEntity } from '../domain/Entities';
import { InjectRepository } from '@nestjs/typeorm';

export default class UserServiceImpl implements UserService {
  constructor(
    @InjectRepository(UserEntity) private repo: Repository<UserEntity>,
  ) {}

  private log = new Logger(UserServiceImpl.name);

  async createUser(user: UserDTO): Promise<boolean> {
    this.log.debug(`Creating user ${user}`);
    const existing = await this.repo.findOneBy({ userId: user.id });

    if (existing) {
      this.log.warn(
        `Failed to create user. User with ID ${user.id} already exists`,
      );
      return false;
    }

    const userToSave = new UserEntity();
    userToSave.name = user.name;
    userToSave.userId = user.id;
    userToSave.lastActive = new Date(Date.now());
    userToSave.thumbnail = user.thumbnailUrl;
    await this.repo.save(userToSave);
    this.log.debug(`Saved user ${userToSave} to db`);
    return true;
  }

  async deleteUser(userId: string): Promise<boolean | null> {
    this.log.debug('Deleting user');
    const result = await this.repo.delete(userId);
    return result.affected !== undefined && result.affected > 0;
  }

  async getUserById(userId: string): Promise<UserDTO | null> {
    this.log.debug(`Getting user with ID ${userId}`);
    const result = await this.repo.findOneBy({
      userId: userId,
    });
    if (result)
      return {
        id: result.userId,
        name: result.name,
        thumbnailUrl: result.thumbnail,
      };
    return null;
  }
}
