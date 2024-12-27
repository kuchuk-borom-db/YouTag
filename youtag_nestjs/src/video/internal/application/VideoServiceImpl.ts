import {VideoService} from '../../api/Services';
import {Inject, Injectable, Logger} from '@nestjs/common';
import {InjectRepository} from '@nestjs/typeorm';
import {VideoEntity} from '../domain/Entities';
import {In, Repository} from 'typeorm';
import {VideoDTO} from 'src/video/api/DTOs';
import {VideoInfoModel} from '../domain/Models';
import {CACHE_MANAGER} from "@nestjs/cache-manager";
import {Cache} from "cache-manager";

@Injectable()
export default class VideoServiceImpl implements VideoService {

    private readonly CACHE_PREFIX = 'video_module:';
    private readonly VIDEO_INFO_URL = 'https://noembed.com/embed?url=https://www.youtube.com/watch?v=';
    private readonly log = new Logger(VideoServiceImpl.name);
    private readonly BATCH_SIZE = 50; // For batch operations

    constructor(
        @Inject(CACHE_MANAGER) private readonly cache: Cache,
        @InjectRepository(VideoEntity) private repo: Repository<VideoEntity>,
    ) {
    }

    // Cache key generators
    private generateVideoCacheKey(videoId: string): string {
        return `${this.CACHE_PREFIX}video:${videoId}`;
    }

    // Cache invalidation
    async invalidateVideosCache(videoIds: string[]): Promise<void> {
        try {
            this.log.debug(`Invalidating cache for videos: ${videoIds}`);

            const cacheKeys = videoIds.map(id => this.generateVideoCacheKey(id));
            // Also invalidate any batch cache keys that might contain these videos
            const batchKeys = await this.cache.store.keys(`${this.CACHE_PREFIX}batch:*`);
            const keysToInvalidate = [...cacheKeys, ...batchKeys];

            await Promise.all(keysToInvalidate.map(key => this.cache.del(key)));
            this.log.debug(`Invalidated ${keysToInvalidate.length} cache entries`);
        } catch (error) {
            this.log.error(`Error invalidating cache: ${error}`);
        }
    }

    // Video operations
    async addVideos(videoIds: string[]): Promise<string[]> {
        const failed: string[] = [];
        this.log.debug(`Adding new videos to database: ${videoIds}`);

        try {
            // Process videos in batches to avoid overwhelming the system
            for (let i = 0; i < videoIds.length; i += this.BATCH_SIZE) {
                const batch = videoIds.slice(i, i + this.BATCH_SIZE);
                const results = await this.processBatchAdd(batch);
                failed.push(...results);
            }
        } catch (error) {
            this.log.error(`Error in batch processing videos: ${error}`);
            return videoIds; // Consider all as failed if batch processing fails
        }

        return failed;
    }

    private async processBatchAdd(videoIds: string[]): Promise<string[]> {
        const failed: string[] = [];
        const fetchPromises = videoIds.map(id => this.getVideoInfo(id));
        const videoInfos = await Promise.allSettled(fetchPromises);

        const entitiesToSave: VideoEntity[] = [];

        for (let i = 0; i < videoIds.length; i++) {
            const videoId = videoIds[i];
            const result = videoInfos[i];

            if (result.status === 'rejected' || !this.isValidVideoInfo(result.status === 'fulfilled' ? result.value : null)) {
                failed.push(videoId);
                continue;
            }

            const vidInfo = result.status === 'fulfilled' ? result.value : null;
            if (!vidInfo) continue;

            const existingVideo = await this.getVideoById(videoId);
            if (existingVideo) {
                this.log.warn(`Video ${videoId} already exists, skipping`);
                continue;
            }

            entitiesToSave.push({
                id: videoId,
                author: vidInfo.channel,
                authorUrl: vidInfo.channelUrl,
                title: vidInfo.title,
                thumbnailUrl: vidInfo.thumbnailUrl,
            });
        }

        if (entitiesToSave.length > 0) {
            try {
                await this.repo.save(entitiesToSave);
                // Invalidate cache for these videos
                await this.invalidateVideosCache(entitiesToSave.map(e => e.id));
            } catch (error) {
                this.log.error(`Error saving videos to database: ${error}`);
                failed.push(...entitiesToSave.map(e => e.id));
            }
        }

        return failed;
    }

    private isValidVideoInfo(info: VideoInfoModel | null): boolean {
        return !!(info && info.title && info.channel && info.thumbnailUrl);
    }

    async removeVideos(videoIds: string[]): Promise<void> {
        if (!videoIds.length) {
            this.log.debug('No videos to remove');
            return;
        }

        try {
            this.log.debug(`Removing videos: ${videoIds}`);

            // Process in batches to avoid overwhelming the database
            for (let i = 0; i < videoIds.length; i += this.BATCH_SIZE) {
                const batch = videoIds.slice(i, i + this.BATCH_SIZE);
                await this.repo.delete(batch);
            }

            await this.invalidateVideosCache(videoIds);
        } catch (error) {
            this.log.error(`Error removing videos: ${error}`, {videoIds});
            throw error; // Propagate error to caller
        }
    }

    private async getVideoInfo(videoId: string): Promise<VideoInfoModel | null> {
        try {
            const response = await fetch(`${this.VIDEO_INFO_URL}${videoId}`, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                },
            });

            if (!response.ok) {
                this.log.error(`Failed to get video info for ${videoId}: ${response.status}`);
                return null;
            }

            const jsonResp = await response.json();
            return {
                channel: jsonResp['author_name'],
                channelUrl: jsonResp['author_url'],
                url: jsonResp['url'],
                thumbnailUrl: jsonResp['thumbnail_url'],
                title: jsonResp['title'],
            };
        } catch (error) {
            this.log.error(`Error fetching video info for ${videoId}:`, error);
            return null;
        }
    }

    async getVideoById(id: string): Promise<VideoDTO | null> {
        const cacheKey = this.generateVideoCacheKey(id);

        try {
            // Try cache first
            const cachedVideo = await this.cache.get<VideoEntity>(cacheKey);
            if (cachedVideo) {
                this.log.debug(`Cache hit for video ${id}`);
                return this.mapToDTO(cachedVideo);
            }

            // Fallback to database
            this.log.debug(`Cache miss for video ${id}, fetching from database`);
            const video = await this.repo.findOneBy({id});
            if (!video) {
                this.log.debug(`Video ${id} not found in database`);
                return null;
            }

            // Cache the result
            await this.cache.set(cacheKey, video,);
            return this.mapToDTO(video);
        } catch (error) {
            this.log.error(`Error retrieving video ${id}:`, error);
            return null;
        }
    }

    async getVideosByIds(ids: string[]): Promise<VideoDTO[]> {
        if (!ids.length) return [];

        try {
            this.log.debug(`Fetching videos: ${ids}`);

            // Get cached videos
            const cachePromises = ids.map(id =>
                this.cache.get<VideoEntity>(this.generateVideoCacheKey(id))
            );
            const cachedResults = await Promise.all(cachePromises);

            // Separate cached and uncached IDs
            const cachedVideos: VideoEntity[] = [];
            const uncachedIds: string[] = [];

            ids.forEach((id, index) => {
                const cached = cachedResults[index];
                if (cached) {
                    cachedVideos.push(cached);
                } else {
                    uncachedIds.push(id);
                }
            });

            // Fetch uncached videos from database
            let dbVideos: VideoEntity[] = [];
            if (uncachedIds.length > 0) {
                dbVideos = await this.repo.find({
                    where: {id: In(uncachedIds)}, order: {
                        title: "ASC"
                    }
                });


                // Cache the database results
                await Promise.all(
                    dbVideos.map(video =>
                        this.cache.set(
                            this.generateVideoCacheKey(video.id),
                            video,
                        )
                    )
                );
            }

            // Combine and map results
            return [...cachedVideos, ...dbVideos].map(this.mapToDTO);
        } catch (error) {
            this.log.error(`Error fetching videos:`, error);
            return [];
        }
    }

    private mapToDTO(entity: VideoEntity): VideoDTO {
        return {
            videoId: entity.id,
            title: entity.title,
            thumbnailUrl: entity.thumbnailUrl,
            author: entity.author,
            authorUrl: entity.authorUrl,
        };
    }
}