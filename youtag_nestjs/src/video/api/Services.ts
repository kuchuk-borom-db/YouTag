import { VideoDTO } from './DTOs';

export abstract class VideoService {
  abstract addVideos(videoIds: string[]): Promise<string[]>;

  abstract removeVideos(videoIds: string[]): Promise<void>;

  abstract getVideoById(id: string): Promise<VideoDTO | null>;
}
