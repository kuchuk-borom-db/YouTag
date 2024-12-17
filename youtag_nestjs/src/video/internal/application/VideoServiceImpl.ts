import { VideoService } from '../../api/Services';
import { Inject, Logger } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { VideoEntity } from '../domain/Entities';
import { Repository } from 'typeorm';
import { VideoDTO } from 'src/video/api/DTOs';
import { VideoInfoModel } from '../domain/Models';
import { CACHE_MANAGER } from '@nestjs/cache-manager';
import { Cache } from 'cache-manager';

export default class VideoServiceImpl extends VideoService {
  constructor(
    @Inject(CACHE_MANAGER) private cache: Cache,
    @InjectRepository(VideoEntity) private repo: Repository<VideoEntity>,
  ) {
    super();
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

        // Immediately cache the video
        const videoDto: VideoDTO = {
          author: vidInfo.channel,
          authorUrl: vidInfo.channelUrl,
          videoId: videoId,
          title: vidInfo.title,
          thumbnailUrl: vidInfo.thumbnailUrl,
        };
        await this.cache.set(this.getVideoCacheKey(videoId), videoDto, 60 * 60); // 1 hour cache
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

  async getVideoById(id: string): Promise<VideoDTO | null> {
    try {
      // Check cache first with proper error handling
      const cacheKey = this.getVideoCacheKey(id);
      const cacheVideo = await this.cache.get<VideoDTO>(cacheKey);

      console.log('Cache Key:', cacheKey);
      console.log('Cached Video:', cacheVideo);

      if (cacheVideo) {
        this.log.debug(`Video found in Cache for ID ${id}`);
        return cacheVideo;
      }

      // If not in cache, fetch from database
      this.log.debug(`Getting video ${id} from database`);
      const vid = await this.repo.findOneBy({ id: id });

      if (!vid) {
        this.log.warn(`Video ${id} not found`);
        return null;
      }

      // Create DTO
      const dbVid: VideoDTO = {
        author: vid.author,
        authorUrl: vid.authorUrl,
        videoId: id,
        title: vid.title,
        thumbnailUrl: vid.thumbnailUrl,
      };

      // Cache the result
      await this.cache.set(cacheKey, dbVid);

      return dbVid;
    } catch (err) {
      this.log.error(`Error at getVideoById ${err}`);
      return null;
    }
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
}
