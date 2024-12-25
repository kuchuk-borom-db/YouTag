import {VideoService} from '../../api/Services';
import {Inject, Logger} from '@nestjs/common';
import {InjectRepository} from '@nestjs/typeorm';
import {VideoEntity} from '../domain/Entities';
import {In, Repository} from 'typeorm';
import {VideoDTO} from 'src/video/api/DTOs';
import {VideoInfoModel} from '../domain/Models';
import {CACHE_MANAGER} from "@nestjs/cache-manager";
import {Cache} from "cache-manager";

export default class VideoServiceImpl implements VideoService {
    constructor(
        @Inject(CACHE_MANAGER) private readonly cache: Cache,
        @InjectRepository(VideoEntity) private repo: Repository<VideoEntity>,
    ) {
    }

    async invalidateVideosCache(videoIds: string[]): Promise<void> {
        this.log.debug(`Invalidating cache of video module for videos ${videoIds}`);
        await Promise.all(videoIds.map(vid => this.cache.del(vid)));
    }


    private videoInfoUrl: string =
        'https://noembed.com/embed?url=https://www.youtube.com/watch?v=';
    private log = new Logger(VideoServiceImpl.name);

    async addVideos(videoIds: string[]): Promise<string[]> {
        const failed: string[] = [];
        this.log.debug(`Adding new videos to database ${videoIds}`);

        const fetchPromises = videoIds.map(id => this.getVideoInfo(id));
        const videoInfos = await Promise.all(fetchPromises);

        for (let i = 0; i < videoIds.length; i++) {
            const videoId = videoIds[i];
            const vidInfo = videoInfos[i];

            if (!vidInfo || vidInfo.title === undefined || vidInfo.title === null) {
                this.log.error(`Failed to get video info ${videoId}`);
                failed.push(videoId);
                continue;
            }

            const existingVideo = await this.getVideoById(videoId);
            if (existingVideo) {
                this.log.warn(`Video ${videoId} already saved. Skipping`);
                continue;
            }

            const videoEntity = {
                author: vidInfo.channel,
                authorUrl: vidInfo.channelUrl,
                title: vidInfo.title,
                thumbnailUrl: vidInfo.thumbnailUrl,
                id: videoId,
            };

            await this.repo.save(videoEntity);
        }

        return failed;
    }


    async removeVideos(videoIds: string[]): Promise<void> {
        this.log.debug(`Remove videos ${videoIds}`);

        if (videoIds.length <= 0) {
            this.log.debug(`Video list is empty`);
            return;
        }
        // Bulk delete from database
        await this.repo.delete(videoIds);
        await this.invalidateVideosCache(videoIds);
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

            const cachedVid = await this.cache.get<VideoEntity>(`video_module_video_${id}`)
            if (cachedVid !== undefined && cachedVid !== null) {
                this.log.debug(`Found cached video ${id}`)
                return {
                    author: cachedVid.author,
                    authorUrl: cachedVid.authorUrl,
                    videoId: id,
                    title: cachedVid.title,
                    thumbnailUrl: cachedVid.thumbnailUrl,
                };
            }
            this.log.debug(`Failed to find cached video ${id}. Getting video from database`)
            const vid = await this.repo.findOneBy({id: id});
            if (!vid) {
                this.log.debug(`Video ${id} not found in database`);
                return null;
            }
            await this.cache.set(`video_module_video_${id}`, vid)
            return {
                author: vid.author,
                authorUrl: vid.authorUrl,
                videoId: id,
                title: vid.title,
                thumbnailUrl: vid.thumbnailUrl,
            };
        } catch (err) {
            this.log.error(`Error at getVideoById ${err}`);
            return null;
        }
    }

    async getVideosByIds(ids: string[]): Promise<VideoDTO[]> {
        this.log.debug(`Getting videos by ids ${ids}`);
        const cachedVids: VideoEntity[] = [];
        for (const id of ids) {
            const cachedVideo = await this.cache.get<VideoEntity>(`video_module_video_${id}`);
            if (cachedVideo) {
                cachedVids.push(cachedVideo);
            }
        }

        const cachedIds = cachedVids.map(vid => vid.id);
        this.log.debug(`Found cached video IDs = ${cachedIds}`);
        const uncachedIds = ids.filter(id => !cachedIds.includes(id));
        this.log.debug(`Videos to be retrieved from DB = ${uncachedIds}`);

        const videosFromDb = await this.repo.findBy({id: In(uncachedIds)});
        await Promise.all(videosFromDb.map(vid => this.cache.set(`video_module_video_${vid.id}`, vid)));

        const result = [...videosFromDb, ...cachedVids];
        return result.map(value => ({
            videoId: value.id,
            title: value.title,
            thumbnailUrl: value.thumbnailUrl,
            author: value.author,
            authorUrl: value.authorUrl,
        }));
    }

}
