import {VideoService} from '../../api/Services';
import {Inject, Logger} from '@nestjs/common';
import {InjectRepository} from '@nestjs/typeorm';
import {VideoEntity} from '../domain/Entities';
import {In, Repository} from 'typeorm';
import {VideoDTO} from 'src/video/api/DTOs';
import {VideoInfoModel} from '../domain/Models';
import {CACHE_MANAGER} from '@nestjs/cache-manager';
import {Cache} from 'cache-manager';

export default class VideoServiceImpl implements VideoService {
    constructor(
        @Inject(CACHE_MANAGER) private cache: Cache,
        @InjectRepository(VideoEntity) private repo: Repository<VideoEntity>,
    ) {
    }


    private videoInfoUrl: string =
        'https://noembed.com/embed?url=https://www.youtube.com/watch?v=';
    private log = new Logger(VideoServiceImpl.name);

    // Improved cache key generation with consistent prefix
    private getVideoCacheKey(videoId: string): string {
        return `video:${videoId}`;
    }

    async addVideos(videoIds: string[]): Promise<string[]> {
        const failed: string[] = [];
        try {
            this.log.debug(`Adding new videos to database ${videoIds}`);

            for (const videoId of videoIds) {
                // Check if video is already saved
                const existingVideo = await this.getVideoById(videoId);
                if (existingVideo) {
                    this.log.warn(`Video ${videoId} already saved. Skipping`);
                    continue;
                }

                // Get video info and save it
                const vidInfo = await this.getVideoInfo(videoId);
                if (!vidInfo || vidInfo.title === undefined || vidInfo.title === null) {
                    this.log.error(`Failed to get video info ${videoId}`);
                    failed.push(videoId);
                    continue;
                }

                // Save video
                const videoEntity = {
                    author: vidInfo.channel,
                    authorUrl: vidInfo.channelUrl,
                    title: vidInfo.title,
                    thumbnailUrl: vidInfo.thumbnailUrl,
                    id: videoId,
                };

                // Save to database
                await this.repo.save(videoEntity);

            }
        } catch (err) {
            this.log.error(`Error at addVideos ${err}`);
        }
        return failed;
    }

    async removeVideos(videoIds: string[]): Promise<void> {
        this.log.debug(`Remove videos ${videoIds}`);

        if (videoIds.length <= 0) {
            this.log.debug(`Video list is empty`);
            return;
        }

        // Bulk remove from cache
        const cacheKeys = videoIds.map((vid) => this.getVideoCacheKey(vid));
        await Promise.all(cacheKeys.map((key) => this.cache.del(key)));

        // Bulk delete from database
        await this.repo.delete(videoIds);
    }


    private async getVideoInfo(videoId: string): Promise<VideoInfoModel | null> {
        try {
            const response = await fetch(`${this.videoInfoUrl}${videoId}`, {
                method: 'GET',
            });

            if (!response.ok) {
                this.log.error(`Failed to get video info ${videoId}`);
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
            this.log.error(`Error fetching video info for ${videoId}`, error);
            return null;
        }
    }

    async getVideoById(id: string): Promise<VideoDTO | null> {
        try {
            const cacheKey = this.getVideoCacheKey(id);
            this.log.debug(`Attempting to get video from cache with key: ${cacheKey}`);

            const cacheVideo = await this.cache.get<VideoDTO>(cacheKey);
            this.log.debug(`Cache result for ${cacheKey}: ${JSON.stringify(cacheVideo)}`);

            if (cacheVideo) {
                this.log.debug(`Cache hit for ID ${id}`);
                return cacheVideo;
            }

            this.log.debug(`Cache miss for ${id}, querying database`);
            const vid = await this.repo.findOneBy({id: id});

            if (!vid) {
                this.log.debug(`Video ${id} not found in database`);
                return null;
            }

            const dbVid: VideoDTO = {
                author: vid.author,
                authorUrl: vid.authorUrl,
                videoId: id,
                title: vid.title,
                thumbnailUrl: vid.thumbnailUrl,
            };

            this.log.debug(`Attempting to cache video with key ${cacheKey}: ${JSON.stringify(dbVid)}`);
            await this.cache.set(cacheKey, dbVid);

            // Verify cache write
            const verifyCached = await this.cache.get<VideoDTO>(cacheKey);
            this.log.debug(`Verify cache write for ${cacheKey}: ${JSON.stringify(verifyCached)}`);

            return dbVid;
        } catch (err) {
            this.log.error(`Error at getVideoById ${err}`);
            return null;
        }
    }

    async getVideosByIds(ids: string[]): Promise<VideoDTO[]> {
        this.log.debug(`Getting videos by ids ${ids}`);

        // Get cached videos using proper cache keys
        const cacheChecks = await Promise.all(
            ids.map(async (id) => {
                const cacheKey = this.getVideoCacheKey(id);
                const cachedValue = await this.cache.get<VideoDTO>(cacheKey);
                this.log.debug(`Cache check for ${cacheKey}: ${JSON.stringify(cachedValue)}`);
                return cachedValue;
            })
        );

        const validCachedVideos = cacheChecks.filter(value => value !== null && value !== undefined);
        this.log.debug(`After null filter valid cached videos = ${JSON.stringify(validCachedVideos)}`);

        const cachedIds = validCachedVideos.map(value => value.videoId);
        this.log.debug(`Cached IDs found: ${JSON.stringify(cachedIds)}`);

        const nonCached = ids.filter(vId => !cachedIds.includes(vId));
        this.log.debug(`Non-cached IDs to fetch from DB: ${JSON.stringify(nonCached)}`);

        const dbInfo = await this.repo.findBy({
            id: In(nonCached)
        });

        const dbVidInfo: VideoDTO[] = dbInfo.map(value => ({
            videoId: value.id,
            title: value.title,
            thumbnailUrl: value.thumbnailUrl,
            author: value.author,
            authorUrl: value.authorUrl
        }));

        // Cache the DB results
        await Promise.all(
            dbVidInfo.map(async (video) => {
                const cacheKey = this.getVideoCacheKey(video.videoId);
                this.log.debug(`Caching DB result with key ${cacheKey}: ${JSON.stringify(video)}`);
                await this.cache.set(cacheKey, video);

                // Verify cache write
                const verifyCached = await this.cache.get<VideoDTO>(cacheKey);
                this.log.debug(`Verify cache write for ${cacheKey}: ${JSON.stringify(verifyCached)}`);
            })
        );

        return dbVidInfo.concat(validCachedVideos);
    }
}
