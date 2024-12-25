import {VideoService} from '../../api/Services';
import {Logger} from '@nestjs/common';
import {InjectRepository} from '@nestjs/typeorm';
import {VideoEntity} from '../domain/Entities';
import {In, Repository} from 'typeorm';
import {VideoDTO} from 'src/video/api/DTOs';
import {VideoInfoModel} from '../domain/Models';


export default class VideoServiceImpl implements VideoService {
    constructor(
        @InjectRepository(VideoEntity) private repo: Repository<VideoEntity>,
    ) {
    }


    private videoInfoUrl: string =
        'https://noembed.com/embed?url=https://www.youtube.com/watch?v=';
    private log = new Logger(VideoServiceImpl.name);

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

            const vid = await this.repo.findOneBy({id: id});

            if (!vid) {
                this.log.debug(`Video ${id} not found in database`);
                return null;
            }

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

        const dbInfo = await this.repo.findBy({
            id: In(ids)
        });

        return dbInfo.map(value => ({
            videoId: value.id,
            title: value.title,
            thumbnailUrl: value.thumbnailUrl,
            author: value.author,
            authorUrl: value.authorUrl
        }));
    }
}
