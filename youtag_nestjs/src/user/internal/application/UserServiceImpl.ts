import { UserDTO } from 'src/user/api/DTOs';
import { UserService } from '../../api/Services';
import { Inject, Logger } from '@nestjs/common';
import { Repository } from 'typeorm';
import { UserEntity } from '../domain/Entities';
import { InjectRepository } from '@nestjs/typeorm';
import { CACHE_MANAGER } from '@nestjs/cache-manager';
import { Cache } from 'cache-manager';

export default class UserServiceImpl implements UserService {
  constructor(
    @Inject(CACHE_MANAGER) private cache: Cache,
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
    await this.cache.del(this.generateUserCacheKey(userId));
    this.log.debug('Deleting user');
    const result = await this.repo.delete(userId);
    return result.affected !== undefined && result.affected > 0;
  }

  async getUserById(userId: string): Promise<UserDTO | null> {
    this.log.debug(`Getting user with ID ${userId}`);
    const cacheUser = await this.cache.get<UserDTO>(
      this.generateUserCacheKey(userId),
    );
    if (cacheUser) {
      this.log.debug(`Found cached user ${JSON.stringify(cacheUser)}`);
      return cacheUser;
    }

    const result = await this.repo.findOneBy({
      userId: userId,
    });
    if (result) {
      const dbUser: UserDTO = {
        id: result.userId,
        name: result.name,
        thumbnailUrl: result.thumbnail,
      };
      this.log.debug(`Found usr in db. Caching it ${userId}`);
      await this.cache.set(this.generateUserCacheKey(userId), dbUser);
      return dbUser;
    }
    return null;
  }

  private generateUserCacheKey(userId: string) {
    return `USER_CACHE_${userId}`;
  }
}
