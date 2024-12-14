import { DataAndTotalCount } from 'src/Utils/Models';
import { TagService } from '../../api/Services';
import { Injectable, Logger } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { TagEntity } from '../domain/Entities';
import { In, Repository } from 'typeorm';

@Injectable()
export default class TagServiceImpl extends TagService {
  constructor(
    @InjectRepository(TagEntity) private repo: Repository<TagEntity>,
  ) {
    super();
  }

  private log = new Logger(TagServiceImpl.name);

  async addTagsToVideos(
    userId: string,
    videoId: string[],
    tags: string[],
  ): Promise<void> {
    try {
      this.log.debug(
        `Adding tags ${tags} to videos ${videoId} for user ${userId}`,
      );
      let tagEntities: TagEntity[] = [];
      videoId.forEach((videoId) => {
        tags.forEach((tag) => {
          const tagToAdd = new TagEntity();
          tagToAdd.tag = tag;
          tagToAdd.userId = userId;
          tagToAdd.videoId = videoId;
          tagEntities.push(tagToAdd);
        });
      });
      await this.repo.save(tagEntities);
    } catch (error) {
      this.log.error(`Error at adding videos to tag ${error}`);
      return;
    }
  }

  async removeTagsFromVideos(
    userId: string,
    videoId: string[],
    tags: string[],
  ): Promise<void> {
    try {
      await this.repo.delete({
        tag: In(tags),
        videoId: In(videoId),
        userId: userId,
      });
    } catch (err) {
      this.log.error('Error while removing tags', err);
      return;
    }
  }

  async getTagsAndCountOfVideo(
    userId: string,
    videoId: string[],
    pagination?: { skip: number; limit: number },
  ): Promise<DataAndTotalCount<string> | null> {
    this.log.debug(
      `Get tags and tag counts of video ${videoId} of user ${userId}`,
    );
    const baseQuery = this.repo
      .createQueryBuilder('entity')
      .select('DISTINCT entity.tag ', 'tag')
      .where('entity.user_id = :user', { user: userId })
      .andWhere('entity.video_id IN (:...videos)', { videos: videoId })
      .printSql();

    const countResult = await baseQuery
      .clone()
      .select('COUNT(DISTINCT entity.tag)', 'count')
      .getRawOne();
    const count = countResult.count;

    if (pagination) {
      baseQuery.skip(pagination.skip).take(pagination.limit);
    }
    const result: any = await baseQuery.getRawMany();
    return {
      datas: result.map((r: any) => r.tag),
      count: Number(count),
    };
  }

  async getTagsAndCountOfUser(
    userId: string,
    pagination?: { skip: number; limit: number },
  ): Promise<DataAndTotalCount<string> | null> {
    try {
      this.log.debug(`Get tags and count of user ${userId}`);
      // First, get unique tags using a subquery
      const queryBuilder = this.repo
        .createQueryBuilder('entity')
        .select('DISTINCT entity.tag', 'tag')
        .where('entity.userId = :userId', { userId });

      // Get total count of unique tags
      const count = (
        await queryBuilder
          .clone()
          .select('COUNT(DISTINCT entity.tag)', 'count')
          .getRawOne()
      ).count;

      // Retrieve paginated unique tags
      let data: string[] = [];
      if (count > 0) {
        const tagsQuery = queryBuilder.orderBy('tag', 'ASC');

        // Apply pagination if specified
        if (pagination) {
          tagsQuery.skip(pagination.skip).take(pagination.limit);
        }

        const result = await tagsQuery.getRawMany();
        data = result.map((item) => item.tag);
      }

      return {
        datas: data,
        count: Number(count),
      };
    } catch (error) {
      this.log.error('Error while retrieving tags', error);
      return null;
    }
  }

  async getVideoIdsAndCountWithTags(
    userId: string,
    tags: string[],
    pagination?: { skip: number; limit: number },
  ): Promise<DataAndTotalCount<string> | null> {
    /*
    REMINDER
    Query Strategy: Find videos with EXACTLY ALL specified tags

    Sample Data Scenario:
      video_id    tag
    1. VID_1       TAG_1
    2. VID_1       TAG_2
    3. VID_1       TAG_3
    4. VID_2       TAG_2
    5. VID_2       TAG_3
    6. VID_2       TAG_4

    Example Scenario:
    - Passed Tags: ['TAG_1', 'TAG_2', 'TAG_4']
    - Expected Output: [] (empty array)


    Conceptual Query Progression:

    1. Initial Data Matching:
       - Database finds rows where:
         a) user_id matches
         b) tag is IN the specified list

       Original Raw Result Set:
       video_id  tag
       VID_1     TAG_1
       VID_1     TAG_2
       VID_2     TAG_2
       VID_2     TAG_4

    2. Aggregation Process (GROUP BY):
       - Collects unique tags per video_id
       - Prepares for tag count verification

       Aggregated Intermediate State:
       video_id  unique_tags    tag_count
       VID_1     [TAG_1, TAG_2]   2
       VID_2     [TAG_2, TAG_4]   2

    3. Final Filtering (HAVING Clause):
       - Checks if number of unique tags matches input tag count
       - Filters out videos not meeting exact tag requirement

       Result:
       - No videos match (because no video has ALL 3 specified tags)
     */
    try {
      this.log.debug(
        `Getting video Ids and counts with tags ${tags} for user ${userId}`,
      );

      // Base query without pagination
      const baseQuery = this.repo
        .createQueryBuilder('entity')
        //select only video id and give it videoId alias
        .select('entity.video_id', 'videoId')
        //userid needs to match
        .where('entity.user_id = :userId', { userId: userId })
        //needs to contain at least one of the tags
        .andWhere('entity.tag IN (:...tags)', { tags: tags })
        //aggregate the result based on video_id
        // Video_id  Unique_tags
        // VID_1     [TAG_1, TAG_2]
        // VID_2     [TAG_2, TAG_4]
        .groupBy('entity.video_id')
        //Check if count matches
        .having('COUNT(DISTINCT entity.tag) = :tagCount', {
          tagCount: tags.length,
        });

      // Query with pagination
      if (pagination) {
        baseQuery.skip(pagination.skip).take(pagination.limit);
      }

      console.log(await this.repo.createQueryBuilder().select("*").getRawMany());

      const count = await this.repo
        .createQueryBuilder()
        .select('COUNT(*)')
        .from("youtag.tags","entity")
        .where('entity.user_id = :userId', { userId: userId }) // Replace with your dynamic user ID
        .andWhere('entity.tag IN (:...tags)', { tags: tags }) // Replace with your dynamic tags array
        .groupBy('entity.video_id')
        .having('COUNT(entity.tag) = :tagCount', { tagCount: tags.length })
        .getRawOne();

      console.log(`RAW COUNT = ${JSON.stringify(count, null, 2)}`);

      const count2 = await this.repo.query(
        `
            SELECT COUNT(*)
            FROM youtag.tags entity
            WHERE entity.user_id = $1
              AND entity.tag IN ($2)
            GROUP BY entity.video_id
            HAVING COUNT(entity.tag) = $3
        `,
        [userId, tags, tags.length],
      );

      console.log(`RAW COUNT 2 = ${JSON.stringify(count2, null, 2)}`);

      // Get paginated video IDs
      const videoIds = await baseQuery.getRawMany<{ videoId: string }>();
      console.log(`Result is ${JSON.stringify(videoIds)}`);

      return {
        datas: videoIds.map((item) => item.videoId),
        count: count.count,
      };
    } catch (error) {
      this.log.error('Error fetching video IDs with tags', error);
      return null;
    }
  }

  async getTaggedVideosOfUser(
    userId: string,
    pagination?: { skip: number; limit: number },
  ): Promise<DataAndTotalCount<string> | null> {
    try {
      this.log.debug(`Get taggedVideosOfUser ${userId}`);
      const baseQuery = this.repo
        .createQueryBuilder('entity')
        .select('DISTINCT entity.video_id', 'videoId')
        .where('entity.user_id = :userId', { userId });

      const count = await baseQuery.getCount();
      if (pagination) {
        baseQuery.skip(pagination.skip).take(pagination.limit);
      }
      const result = await baseQuery.getRawMany();
      return {
        datas: result.map((r) => r.videoId),
        count: count,
      };
    } catch (error) {
      this.log.error(`Error at Get Tagged videos of user ${userId} ${error}`);
      return null;
    }
  }

  async getTagsAndCountContaining(
    userId: string,
    containing: string,
    pagination?: { skip: number; limit: number },
  ): Promise<DataAndTotalCount<string> | null> {
    try {
      this.log.debug(
        `Get tags and count containing ${containing} of user ${userId}`,
      );

      // Base Query: Fetch unique tags
      const baseQuery = this.repo
        .createQueryBuilder('entity')
        .select('DISTINCT entity.tag', 'tag')
        .where('entity.user_id = :userId', { userId })
        .andWhere('entity.tag LIKE :keyword', { keyword: `%${containing}%` });

      // Get total count of unique tags
      const count = await baseQuery.clone().getCount();

      // Apply pagination if specified
      if (pagination) {
        baseQuery.skip(pagination.skip).take(pagination.limit);
      }

      // Execute query to get paginated results
      const result = await baseQuery.getRawMany();

      return {
        datas: result.map((r) => r.tag),
        count: count,
      };
    } catch (error) {
      this.log.error(
        `Error in get tags and count containing ${containing} of user ${userId} and pagination ${pagination}`,
        error,
      );
      return null;
    }
  }
}
