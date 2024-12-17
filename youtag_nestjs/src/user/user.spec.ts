import { Test } from '@nestjs/testing';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { EnvironmentConst } from '../Utils/Constants';
import { UserEntity } from './internal/domain/Entities';
import UserServiceImpl from './internal/application/UserServiceImpl';
import { DataSource } from 'typeorm';
import { AuthJwtService, AuthService } from './api/Services';
import { AuthJwtServiceImpl } from './internal/application/AuthJWTServiceImpl';
import { JwtModule } from '@nestjs/jwt';
import { GoogleAuthServiceImpl } from './internal/application/GoogleOAuthService';
import { CacheModule } from '@nestjs/cache-manager';

describe(`User Service Integration TEST`, () => {
  let service: UserServiceImpl;
  let db: DataSource;
  beforeAll(async () => {
    const module = await Test.createTestingModule({
      imports: [
        CacheModule.register(),
        //Imports for Database to be configured and injected
        await ConfigModule.forRoot(),
        TypeOrmModule.forRootAsync({
          imports: [ConfigModule],
          inject: [ConfigService],
          useFactory: async (configService: ConfigService) => {
            return {
              type: 'postgres',
              host: configService.get<string>(EnvironmentConst.Db.Host),
              username: configService.get<string>(EnvironmentConst.Db.Username),
              password: configService.get<string>(EnvironmentConst.Db.Password),
              database: configService.get<string>(
                EnvironmentConst.Db.DatabaseName,
              ),
              ssl: {
                rejectUnauthorized: false,
              },
              entities: [UserEntity],
            };
          },
        }),
        //Import for UserService
        TypeOrmModule.forFeature([UserEntity]),
      ],
      //Providers to create. This will be successfully created as it's dependency is imported
      providers: [UserServiceImpl],
    }).compile();

    service = module.get<UserServiceImpl>(UserServiceImpl);
    db = module.get(DataSource);
  });

  afterAll(async () => {
    await db.destroy();
  });

  afterEach(async () => {
    await db.getRepository(UserEntity).delete('test');
  });

  const user: UserEntity = {
    userId: 'test',
    name: 'test name',
    lastActive: new Date(Date.now()),
    thumbnail: 'NONE',
  };

  it('Creating user using repository should succeed', async () => {
    const userToCreate = new UserEntity();
    userToCreate.name = user.name;
    userToCreate.lastActive = new Date(Date.now());
    userToCreate.userId = user.userId;
    userToCreate.thumbnail = user.thumbnail;
    await db.getRepository(UserEntity).save(userToCreate);
    expect(true).toBe(true);
  });

  it('Create user should succeed', async () => {
    const success = await service.createUser({
      id: user.userId,
      name: user.name,
      thumbnailUrl: user.thumbnail,
    });
    expect(success).toBe(true);
    //Check if user was created
    const userFound = await db
      .getRepository(UserEntity)
      .findOne({ where: { userId: user.userId } });
    expect(userFound).toBeDefined();
    expect(userFound.name).toBe(user.name);
  });

  it('Create user should fail', async () => {
    const userToCreate = new UserEntity();
    userToCreate.name = user.name;
    userToCreate.lastActive = new Date(Date.now());
    userToCreate.userId = user.userId;
    userToCreate.thumbnail = user.thumbnail;
    await db.getRepository(UserEntity).save(userToCreate);

    const result = await service.createUser({
      thumbnailUrl: user.thumbnail,
      name: user.name,
      id: user.userId,
    });
    expect(result).toBe(false);
  });

  it('Get user should return null', async () => {
    const user = await service.getUserById('NOPE');
    expect(user).toBeNull();
  });

  it('Get user should return valid user', async () => {
    const success = await service.createUser({
      id: user.userId,
      name: user.name,
      thumbnailUrl: user.thumbnail,
    });
    expect(success).toBe(true);

    const found = await service.getUserById(user.userId);
    expect(found).toBeDefined();
    expect(found.name).toBe(user.name);
  });

  it('Delete user should delete', async () => {
    const success = await service.createUser({
      id: user.userId,
      name: user.name,
      thumbnailUrl: user.thumbnail,
    });
    expect(success).toBe(true);

    const found = await service.getUserById(user.userId);
    expect(found).toBeDefined();
    expect(found.name).toBe(user.name);

    const result = await service.deleteUser(user.userId);
    expect(result).toBe(true);
  });

  it('Delete user should fail', async () => {
    const result = await service.deleteUser(user.userId);
    expect(result).toBe(false);
  });
});
describe(`${AuthService.name}`, () => {
  let authService: AuthService;

  beforeAll(async () => {
    const model = await Test.createTestingModule({
      imports: [JwtModule, await ConfigModule.forRoot()],
      providers: [
        {
          provide: AuthJwtService,
          useClass: AuthJwtServiceImpl,
        },
        {
          provide: AuthService,
          useClass: GoogleAuthServiceImpl,
        },
      ],
    }).compile();

    authService = model.get(AuthService);
  });

  it('Should return valid login url', async () => {
    const url = await authService.getOAuthLoginURL();
    console.log(url);
    expect(url).toBeDefined();
  });

  it('Should get user info', async () => {
    const code =
      '4/0AanRRrvL-YkB3_eI4bA2rfjRUoqOyJJVv7glXD-GQpuOHiKI3glLTHG38cpxvVbX17ipnw\n';
    const userInfo = await authService.getOAuthUserInfo(code);
    console.log(userInfo);
    expect(userInfo).toBeDefined();
  });
});
