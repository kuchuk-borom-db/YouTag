import { VideoService } from './api/Services';
import { Test } from '@nestjs/testing';
import VideoServiceImpl from './internal/application/VideoServiceImpl';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { EnvironmentConst } from '../Utils/Constants';
import { DataSource, Repository } from 'typeorm';
import { VideoEntity } from './internal/domain/Entities';

describe(`Video Service Integration Test`, () => {
  let videoService: VideoService;
  let repo: Repository<VideoEntity>;
  let db: DataSource;
  beforeAll(async () => {
    const module = await Test.createTestingModule({
      providers: [
        {
          provide: VideoService,
          useClass: VideoServiceImpl,
        },
      ],
      imports: [
        await ConfigModule.forRoot(),
        TypeOrmModule.forRootAsync({
          imports: [ConfigModule],
          inject: [ConfigService],
          useFactory: (env: ConfigService) => {
            return {
              type: 'postgres',
              host: env.get<string>(EnvironmentConst.Db.Host),
              username: env.get<string>(EnvironmentConst.Db.Username),
              password: env.get<string>(EnvironmentConst.Db.Password),
              database: env.get<string>(EnvironmentConst.Db.DatabaseName),
              ssl: {
                rejectUnauthorized: true,
              },
              entities: [VideoEntity],
              //synchronize: true, //only for development
            };
          },
        }),
        TypeOrmModule.forFeature([VideoEntity]),
      ],
    }).compile();
    videoService = module.get(VideoService);
    db = module.get<DataSource>(DataSource);
  });

  beforeEach(async () => {
    repo = db.getRepository(VideoEntity);
  });

  afterEach(async () => {
    await repo.delete(id);
  });

  afterAll(async () => {
    await db.destroy();
  });

  const id = '-wLYuox7YE8';

  describe('Get video', () => {
    it('should fail to get video', async () => {
      const video = await videoService.getVideoById(id);
      expect(video).toBeNull();
    });

    it('Should get video', async () => {
      await repo.save({
        id: id,
        title: 'RANDOM',
        authorUrl: 'ASD',
        thumbnailUrl: 'ASD',
        author: 'ASD',
      });
      const video = await videoService.getVideoById(id);
      expect(video).toBeDefined();
    });
  });

  describe('Save video', () => {
    it('should save video', async () => {
      const saved = await videoService.addVideo(id);
      expect(saved).toBeDefined();
      expect(saved.title).toBeDefined();

      const result = await repo.findOneBy({ id: id });
      expect(result).toBeDefined();
      expect(result.title).toBe(saved.title);
    });

    it('Should fail to add video', async () => {
      await repo.save({
        id: id,
        title: 'RANDOM',
        authorUrl: 'ASD',
        thumbnailUrl: 'ASD',
        author: 'ASD',
      });
      const saved = await videoService.addVideo(id);
      expect(saved).toBeNull();
    });
  });

  it('Remove video', async () => {
    await repo.save({
      id: id,
      title: 'RANDOM',
      authorUrl: 'ASD',
      thumbnailUrl: 'ASD',
      author: 'ASD',
    });
    await videoService.removeVideos(id);
    const found = await repo.findOneBy({ id: id });
    expect(found).toBeNull();
  });
});
