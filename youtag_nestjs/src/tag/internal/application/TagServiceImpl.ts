import {DataAndTotalCount} from 'src/Utils/Models';
import {TagService} from '../../api/Services';
import {Inject, Injectable, Logger} from '@nestjs/common';
import {InjectRepository} from '@nestjs/typeorm';
import {TagEntity} from '../domain/Entities';
import {In, Repository} from 'typeorm';
import {CACHE_MANAGER} from "@nestjs/cache-manager";
import {Cache} from "cache-manager";

@Injectable()
export default class TagServiceImpl extends TagService {
    private readonly CACHE_PREFIX = 'tag_service:';
    private log = new Logger(TagServiceImpl.name);

    constructor(
        @Inject(CACHE_MANAGER) private readonly cache: Cache,
        @InjectRepository(TagEntity) private repo: Repository<TagEntity>,
    ) {
        super();
    }

    // Cache key generators
    private generateTagsOfVideoKey(userId: string, videoIds: string[]): string {
        return `${this.CACHE_PREFIX}tags_of_video:${userId}:${videoIds.sort().join(',')}`;
    }

    private generateUserTagsKey(userId: string): string {
        return `${this.CACHE_PREFIX}user_tags:${userId}`;
    }

    private generateVideosByTagsKey(userId: string, tags: string[]): string {
        return `${this.CACHE_PREFIX}videos_by_tags:${userId}:${tags.sort().join(',')}`;
    }

    private generateTaggedVideosKey(userId: string): string {
        return `${this.CACHE_PREFIX}tagged_videos:${userId}`;
    }

    private generateTagsContainingKey(userId: string, containing: string): string {
        return `${this.CACHE_PREFIX}tags_containing:${userId}:${containing}`;
    }

    // Cache invalidation
    async invalidateUserVideoCache(userId: string, videos: string[]): Promise<void> {
        try {
            const keys = await this.cache.store.keys(`${this.CACHE_PREFIX}*`);
            const keysToDelete: string[] = [];

            for (const key of keys) {
                // Check if key contains userId
                if (key.includes(userId)) {
                    // For keys containing specific video IDs, check if they contain any of the videos
                    if (videos.some(video => key.includes(video))) {
                        keysToDelete.push(key);
                    }
                    // Also invalidate user-wide caches
                    if (key.includes('user_tags') ||
                        key.includes('tagged_videos') ||
                        key.includes('tags_containing')) {
                        keysToDelete.push(key);
                    }
                }
            }

            await Promise.all(keysToDelete.map(key => this.cache.del(key)));
            this.log.debug(`Invalidated ${keysToDelete.length} cache keys for user ${userId}`);
        } catch (error) {
            this.log.error(`Error invalidating cache: ${error}`);
        }
    }

    // Helper method for pagination
    private applyPagination<T>(data: DataAndTotalCount<T> | null, pagination?: {
        skip: number;
        limit: number
    }): DataAndTotalCount<T> | null {
        if (!data || !pagination) return data;

        return {
            datas: data.datas.slice(pagination.skip, pagination.skip + pagination.limit),
            count: data.count
        };
    }

    // Implementation methods
    async addTagsToVideos(
        userId: string,
        videoIds: string[],
        tags: string[],
    ): Promise<void> {
        try {
            this.log.debug(
                `Adding tags ${tags} to videos ${videoIds} for user ${userId}`,
            );

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
                .orIgnore()
                .execute();

            await this.invalidateUserVideoCache(userId, videoIds);
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
            await this.invalidateUserVideoCache(userId, videoId);
        } catch (err) {
            this.log.error('Error while removing tags', err);
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
            await this.invalidateUserVideoCache(userId, videoId);
        } catch (error) {
            this.log.error(`Error removing tags from videos: ${error}`);
        }
    }

    async getTagsAndCountOfVideo(
        userId: string,
        videoId: string[],
        pagination?: { skip: number; limit: number },
    ): Promise<DataAndTotalCount<string> | null> {
        const cacheKey = this.generateTagsOfVideoKey(userId, videoId);

        try {
            // Try to get from cache
            const cached = await this.cache.get<DataAndTotalCount<string>>(cacheKey);
            if (cached) {
                this.log.debug(`Cache hit for ${cacheKey}`);
                return this.applyPagination(cached, pagination);
            }

            this.log.debug(
                `Get tags and tag counts of video ${videoId} of user ${userId}`,
            );

            const baseQuery = this.repo
                .createQueryBuilder('entity')
                .select('DISTINCT entity.tag ', 'tag')
                .where('entity.user_id = :user', {user: userId})
                .andWhere('entity.video_id IN (:...videos)', {videos: videoId});

            const countResult = await baseQuery
                .clone()
                .select('COUNT(DISTINCT entity.tag)', 'count')
                .getRawOne();

            const result = await baseQuery.getRawMany();
            const data = {
                datas: result.map((r: any) => r.tag),
                count: Number(countResult.count),
            };

            await this.cache.set(cacheKey, data);
            return this.applyPagination(data, pagination);
        } catch (error) {
            this.log.error(`Error in getTagsAndCountOfVideo: ${error}`);
            return null;
        }
    }

    async getTagsAndCountOfUser(
        userId: string,
        pagination?: { skip: number; limit: number },
    ): Promise<DataAndTotalCount<string> | null> {
        const cacheKey = this.generateUserTagsKey(userId);

        try {
            // Try to get from cache
            const cached = await this.cache.get<DataAndTotalCount<string>>(cacheKey);
            if (cached) {
                this.log.debug(`Cache hit for ${cacheKey}`);
                return this.applyPagination(cached, pagination);
            }

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
                const result = await queryBuilder
                    .orderBy('tag', 'ASC')
                    .getRawMany();
                data = result.map((item) => item.tag);
            }

            const resultData = {
                datas: data,
                count: Number(count),
            };

            await this.cache.set(cacheKey, resultData, );
            return this.applyPagination(resultData, pagination);
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
        const cacheKey = this.generateVideosByTagsKey(userId, tags);

        try {
            // Try to get from cache
            const cached = await this.cache.get<DataAndTotalCount<string>>(cacheKey);
            if (cached) {
                this.log.debug(`Cache hit for ${cacheKey}`);
                return this.applyPagination(cached, pagination);
            }

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

            const videoIds = await baseQuery.getRawMany<{ videoId: string }>();

            const resultData = {
                datas: videoIds.map((item) => item.videoId),
                count: parseInt(count),
            };

            await this.cache.set(cacheKey, resultData, );
            return this.applyPagination(resultData, pagination);
        } catch (error) {
            this.log.error('Error fetching video IDs with tags', error);
            return null;
        }
    }

    async getTaggedVideosOfUser(
        userId: string,
        pagination?: { skip: number; limit: number },
    ): Promise<DataAndTotalCount<string> | null> {
        const cacheKey = this.generateTaggedVideosKey(userId);

        try {
            // Try to get from cache
            const cached = await this.cache.get<DataAndTotalCount<string>>(cacheKey);
            if (cached) {
                this.log.debug(`Cache hit for ${cacheKey}`);
                return this.applyPagination(cached, pagination);
            }

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

            const result = await baseQuery.getRawMany();

            const resultData = {
                datas: result.map((r) => r.videoId),
                count: count,
            };

            await this.cache.set(cacheKey, resultData, );
            return this.applyPagination(resultData, pagination);
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
        const cacheKey = this.generateTagsContainingKey(userId, containing);

        try {
            // Try to get from cache
            const cached = await this.cache.get<DataAndTotalCount<string>>(cacheKey);
            if (cached) {
                this.log.debug(`Cache hit for ${cacheKey}`);
                return this.applyPagination(cached, pagination);
            }

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

            const result = await baseQuery.getRawMany();

            const resultData = {
                datas: result.map((r) => r.tag),
                count: count,
            };

            await this.cache.set(cacheKey, resultData,);
            return this.applyPagination(resultData, pagination);
        } catch (error) {
            this.log.error(
                `Error in get tags and count containing ${containing} of user ${userId}`,
                error,
            );
            return null;
        }
    }

    async getVideosNotInUse(videoIds: string[]): Promise<string[]> {
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
            return [];
        }
    }



}