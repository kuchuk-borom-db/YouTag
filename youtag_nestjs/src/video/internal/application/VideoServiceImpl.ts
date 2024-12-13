import { VideoService } from '../../api/Services';
import { Logger } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { VideoEntity } from '../domain/Entities';
import { Repository } from 'typeorm';
import { VideoDTO } from 'src/video/api/DTOs';
import { VideoInfoModel } from '../domain/Models';

export default class VideoServiceImpl extends VideoService {
  constructor(
    @InjectRepository(VideoEntity) private repo: Repository<VideoEntity>,
  ) {
    super();
  }

  private videoInfoUrl: string =
    'https://noembed.com/embed?url=https://www.youtube.com/watch?v=';
  private log = new Logger(VideoServiceImpl.name);

  async addVideo(videoId: string): Promise<VideoDTO> {
    try {
      this.log.debug(`Adding new video to database ${videoId}`);
      //Check if video is already saved
      const existingVideo = await this.getVideoById(videoId);
      if (existingVideo) {
        this.log.error(`Video ${videoId} already saved`);
        return null;
      }
      //Get video info and save it
      const vidInfo = await this.getVideoInfo(videoId);
      if (!vidInfo) {
        this.log.error(`Failed to get video info ${videoId}`);
        return null;
      }
      //Save video
      const saved = await this.repo.save({
        author: vidInfo.channel,
        authorUrl: vidInfo.channelUrl,
        title: vidInfo.title,
        thumbnailUrl: vidInfo.thumbnailUrl,
        id: videoId,
      });
      return {
        author: saved.author,
        authorUrl: saved.authorUrl,
        videoId: saved.id,
        title: saved.title,
        thumbnailUrl: saved.thumbnailUrl,
      };
    } catch (err) {
      this.log.error(`Error at addVideo ${err}`);
      return null;
    }
  }

  async removeVideo(videoId: string): Promise<void> {
    this.log.debug(`Remove video ${videoId}`);
    await this.repo.delete(videoId);
  }

  async getVideoById(id: string): Promise<VideoDTO | null> {
    try {
      this.log.debug(`Getting video ${id}`);
      const vid = await this.repo.findOneBy({ id: id });
      if (!vid) {
        this.log.warn(`Video ${id} not found`);
        return null;
      }
      this.log.debug(`Got video ${vid}`);
      return {
        author: vid.author,
        authorUrl: vid.authorUrl,
        videoId: id,
        title: vid.title,
        thumbnailUrl: vid.thumbnailUrl,
      };
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
}
