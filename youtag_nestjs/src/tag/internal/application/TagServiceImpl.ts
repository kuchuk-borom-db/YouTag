import {DataAndTotalCount} from 'src/Utils/Models';
import {TagService} from '../../api/Services';
import {Injectable, Logger} from '@nestjs/common';
import {InjectRepository} from '@nestjs/typeorm';
import {TagEntity} from '../domain/Entities';
import {In, Repository} from 'typeorm';

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
        videoIds: string[],
        tags: string[],
    ): Promise<void> {
        try {
            this.log.debug(
                `Adding tags ${tags} to videos ${videoIds} for user ${userId}`,
            );

            // Create values for bulk insert
            const values = videoIds.flatMap(videoId =>
                tags.map(tag => ({
                    userId,
                    videoId,
                    tag
                }))
            );

            await this.repo
                .createQueryBuilder()
                .insert()
                .into(TagEntity)
                .values(values)
                .orIgnore() // This adds ON CONFLICT DO NOTHING
                .execute();

        } catch (error) {
            this.log.error(`Error while adding tags to videos: ${error}`);
        }
    }

    async removeTagsFromVideos(
        userId: string,
        videoId: string[],
        tags: string[],
    ): Promise<void> {
        try {
            this.log.debug(
                `Removing tags ${tags} from videos ${videoId} of user ${userId}`,
            );
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

    async removeAllTagsFromVideos(
        userId: string,
        videoId: string[],
    ): Promise<void> {
        try {
            this.log.debug(
                `Removing all tags from videos ${videoId} of user ${userId}`,
            );

            const result = await this.repo.delete({
                videoId: In(videoId),
                userId: userId,
            });

            this.log.debug(`Removed ${result.affected} tag entries`);
        } catch (error) {
            this.log.error(`Error removing tags from videos: ${error}`);
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
            .where('entity.user_id = :user', {user: userId})
            .andWhere('entity.video_id IN (:...videos)', {videos: videoId})
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
            const queryBuilder = this.repo
                .createQueryBuilder('entity')
                .select('DISTINCT entity.tag', 'tag')
                .where('entity.userId = :userId', {userId});

            const count = (
                await queryBuilder
                    .clone()
                    .select('COUNT(DISTINCT entity.tag)', 'count')
                    .getRawOne()
            ).count;

            let data: string[] = [];
            if (count > 0) {
                const tagsQuery = queryBuilder.orderBy('tag', 'ASC');

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


         RAW QUERY
         SELECT t.video_id FROM youtag.tags t
         where t.user_id = 'user'
         AND t.tag  IN ('tag_1', 'tag_2')
         GROUP BY t.video_id
         HAVING COUNT(t.tag) = 2;

         We use count to count the no. of rows returned as result from the sub query
         SELECT COUNT(sq.vide0_id) FROM (SELECT t.video_id FROM youtag.tags t
         where t.user_id = 'user'
         AND t.tag  IN ('tag_1', 'tag_2')
         GROUP BY t.video_id
         HAVING COUNT(t.tag) = 2) AS sq;
       */

        try {
            this.log.debug(
                `Getting video Ids and counts with tags ${tags} for user ${userId}`,
            );

            const baseQuery = this.repo
                .createQueryBuilder('entity')
                .select('entity.video_id', 'videoId')
                .where('entity.user_id = :userId', {userId: userId})
                .andWhere('entity.tag IN (:...tags)', {tags: tags})
                .groupBy('entity.video_id')
                .having('COUNT(DISTINCT entity.tag) = :tagCount', {
                    tagCount: tags.length,
                });

            const count = (
                await this.repo.query(
                    `
                        SELECT COUNT(*)
                        FROM (SELECT t.video_id
                              FROM youtag.tags t
                              WHERE t.user_id = $1
                                AND t.tag = ANY ($2)
                              GROUP BY t.video_id
                              HAVING COUNT(DISTINCT t.tag) = $3) AS subquery
                    `,
                    [userId, tags, tags.length],
                )
            )[0].count;

            if (pagination) {
                baseQuery.skip(pagination.skip).take(pagination.limit);
            }

            const videoIds = await baseQuery.getRawMany<{ videoId: string }>();

            return {
                datas: videoIds.map((item) => item.videoId),
                count: parseInt(count),
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
                .where('entity.user_id = :userId', {userId})
                .groupBy('entity.video_id');

            const count = parseInt(
                (
                    await this.repo
                        .createQueryBuilder('entity')
                        .select('COUNT(DISTINCT entity.video_id)', 'count')
                        .where('entity.user_id = :userId', {userId: userId})
                        .getRawOne()
                ).count,
            );

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

            const baseQuery = this.repo
                .createQueryBuilder('entity')
                .select('DISTINCT entity.tag', 'tag')
                .where('entity.user_id = :userId', {userId})
                .andWhere('entity.tag LIKE :keyword', {keyword: `%${containing}%`});

            const count = parseInt(
                (
                    await this.repo
                        .createQueryBuilder('entity')
                        .select('COUNT(DISTINCT entity.tag)', 'tag')
                        .where('entity.user_id = :userId', {userId})
                        .andWhere('entity.tag LIKE :keyword', {
                            keyword: `%${containing}%`,
                        })
                        .getRawOne()
                ).tag,
            );

            if (pagination) {
                baseQuery.skip(pagination.skip).take(pagination.limit);
            }

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

    async getVideosNotInUse(videoIds: string[]): Promise<string[]> {
        // If no videoIds provided, return empty array to avoid SQL syntax error
        if (!videoIds || videoIds.length === 0) {
            return [];
        }

        try {
            this.log.debug(`Getting videos not in use from list ${videoIds}`);
            const rawVideos = await this.repo
                .createQueryBuilder('entity')
                .select('DISTINCT entity.video_id', 'videoId')
                .where('entity.video_id = ANY(:videoIds)', {videoIds: videoIds})
                .getRawMany();

            const foundVideos: string[] = rawVideos.map((r) => r.videoId);
            return videoIds.filter((v) => !foundVideos.includes(v));
        } catch (error) {
            this.log.error(`Error getting videos not in use`, {
                error,
                videoIds
            });
        }
    }
}
