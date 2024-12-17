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

  async addVideos(videoIds: string[]): Promise<string[]> {
    const failed: string[] = [];
    try {
      this.log.debug(`Adding new videos to database ${videoIds}`);
      //TODO Any way to skip iterating and add in bulk while getting info? Maybe updating info only when we need to get the video?
      for (const videoId of videoIds) {
        //Check if video is already saved
        const existingVideo = await this.getVideoById(videoId);
        if (existingVideo) {
          this.log.warn(`Video ${videoId} already saved. Skipping`);
          continue;
        }
        //Get video info and save it
        const vidInfo = await this.getVideoInfo(videoId);
        if (!vidInfo || vidInfo.title === undefined || vidInfo.title === null) {
          this.log.error(`Failed to get video info ${videoId}`);
          failed.push(videoId);
          continue;
        }
        //Save video
        await this.repo.save({
          author: vidInfo.channel,
          authorUrl: vidInfo.channelUrl,
          title: vidInfo.title,
          thumbnailUrl: vidInfo.thumbnailUrl,
          id: videoId,
        });
      }
    } catch (err) {
      this.log.error(`Error at addVideos ${err}`);
    }
    return failed;
  }

  async removeVideos(videoId: string[]): Promise<void> {
    this.log.debug(`Remove video ${videoId}`);
    for (const vid in videoId) {
      await this.cache.del(this.getVideoCacheKey(vid));
    }
    if (videoId.length <= 0) {
      this.log.debug(`Video is empty`);
      return;
    }
    await this.repo.delete(videoId);
  }

  async getVideoById(id: string): Promise<VideoDTO | null> {
    try {
      const cacheVideo = await this.cache.get<VideoDTO>(
        this.getVideoCacheKey(id),
      );
      if (cacheVideo) {
        this.log.debug(`Video found in Cache ${cacheVideo}`);
        return cacheVideo;
      }
      this.log.debug(`Getting video ${id} rom database`);
      const vid = await this.repo.findOneBy({ id: id });
      if (!vid) {
        this.log.warn(`Video ${id} not found`);
        return null;
      }
      this.log.debug(`Got video ${vid}`);
      const dbVid: VideoDTO = {
        author: vid.author,
        authorUrl: vid.authorUrl,
        videoId: id,
        title: vid.title,
        thumbnailUrl: vid.thumbnailUrl,
      };
      this.log.debug(`Got video ${vid}. Caching it.`);
      await this.cache.set(this.getVideoCacheKey(id), dbVid);
      return dbVid;
    } catch (err) {
      this.log.error(`Error at getVideoByID ${err}`);
      return null;
    }
  }

  private async getVideoInfo(videoId: string): Promise<VideoInfoModel | null> {
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
  }

  private getVideoCacheKey(videoId: string): string {
    return `VIDEO_${videoId}`;
  }
}
