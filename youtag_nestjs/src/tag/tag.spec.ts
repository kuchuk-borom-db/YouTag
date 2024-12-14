import { DataSource, Repository } from 'typeorm';
import { TagService } from './api/Services';
import { TagEntity } from './internal/domain/Entities';
import { Test } from '@nestjs/testing';
import TagServiceImpl from './internal/application/TagServiceImpl';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { EnvironmentConst } from '../Utils/Constants';

describe('Tag Integration test', () => {
  let service: TagService;
  let db: DataSource;
  let repo: Repository<TagEntity>;

  const getTotalCount = async () => {
    return await repo
      .createQueryBuilder()
      .select('*')
      .where('video_id IN (:...videos)', {
        videos: ['video_1', 'video_2', 'video_3'],
      })
      .getCount();
  };

  const addDefault = async () => {
    await service.addTagsToVideos(
      'user',
      ['video_1', 'video_2', 'video_3'],
      ['tag_1', 'tag_2', 'tag_3'],
    );
  };

  beforeAll(async () => {
    const module = await Test.createTestingModule({
      providers: [
        {
          provide: TagService,
          useClass: TagServiceImpl,
        },
      ],
      imports: [
        await ConfigModule.forRoot({}),
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
              entities: [TagEntity],
            };
          },
        }),
        TypeOrmModule.forFeature([TagEntity]),
      ],
    }).compile();

    service = module.get(TagService);
    db = module.get(DataSource);
    repo = db.getRepository(TagEntity);
  });
  beforeEach(async () => {
    await repo.delete({ userId: 'user' });
  });
  afterEach(async () => {
    //await repo.delete({ userId: 'user' });
    await addDefault();
  });
  afterAll(async () => {
    await db.destroy();
  });

  it("TEST",async ()=>{
    expect(true).toBe(true);
  })

  describe('Add tags to video', () => {
    it('should add tags to videos', async () => {
      await addDefault();

      const added = await getTotalCount();

      expect(added).toBeDefined();
      expect(added).toBe(9);
    });

    it('should add tags to videos', async () => {
      await addDefault();

      await service.addTagsToVideos('user', ['video_1'], ['tag_1']);
      const added = await getTotalCount();

      expect(added).toBeDefined();
      expect(added).toBe(9);
    });
  });

  describe('Remove tags from videos', () => {
    it('Should remove tags from videos', async () => {
      await addDefault();

      await service.removeTagsFromVideos(
        'user',
        ['video_1'],
        ['tag_1', 'tag_2', 'tag_3'],
      );

      const count = await getTotalCount();
      expect(count).toBeDefined();
      expect(count).toBe(6);
    });

    it('Should not remove tags from videos', async () => {
      await addDefault();

      await service.removeTagsFromVideos('user', [], []);

      const count = await getTotalCount();
      expect(count).toBeDefined();
      expect(count).toBe(9);
    });
  });

  describe('Get tags and count of video', () => {
    it('Should get 3 tags from all videos and pass pagination test', async () => {
      await addDefault();

      let tags = await service.getTagsAndCountOfVideo('user', [
        'video_1',
        'video_2',
        'video_3',
      ]);
      expect(tags).toBeDefined();
      expect(tags.datas).toHaveLength(3);
      expect(tags.count).toBe('3');

      tags = await service.getTagsAndCountOfVideo(
        'user',
        ['video_1', 'video_2', 'video_3'],
        {
          skip: 0,
          limit: 2,
        },
      );

      expect(tags).toBeDefined();
      expect(tags.datas).toHaveLength(2);
      expect(tags.count).toBe(3);
    });
  });

  describe('Get tags and count of user', () => {
    it('Should get all tags and count', async () => {
      await addDefault();

      let result = await service.getTagsAndCountOfUser('user');
      expect(result).toBeDefined();
      expect(result.datas).toHaveLength(3);
      expect(result.count).toBe(3);

      result = await service.getTagsAndCountOfUser('user', {
        skip: 2,
        limit: 2,
      });
      expect(result).toBeDefined();
      expect(result.datas).toHaveLength(1);
      expect(result.count).toBe(3);
    });
  });

  describe('Get Video IDs and Count with tags', () => {
    it('should get all video ids and count with tag_1', async () => {
      await addDefault();
      //await service.addTagsToVideos('user', ['video_1'], ['extra']);

      let result = await service.getVideoIdsAndCountWithTags('user', ['tag_1']);
      expect(result).toBeDefined();
      expect(result.datas).toHaveLength(3);
      expect(result.count).toBe(3);

      result = await service.getVideoIdsAndCountWithTags('user', [
        'tag_1',
        'tag_2',
      ]);
      expect(result).toBeDefined();
      expect(result.datas).toHaveLength(3);
      expect(result.count).toBe(3);

      result = await service.getVideoIdsAndCountWithTags('user', [
        'tag_1',
        'tag_2',
        'extra',
      ]);
      expect(result).toBeDefined();
      expect(result.datas).toHaveLength(1);
      expect(result.count).toBe(1);

      result = await service.getVideoIdsAndCountWithTags('user', [
        'tag_1',
        'tag_2',
        'extra',
        'extra2',
      ]);
      expect(result).toBeDefined();
      expect(result.datas).toHaveLength(0);
      expect(result.count).toBe(0);
    });
  });
});
