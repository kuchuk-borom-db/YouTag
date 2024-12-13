import { VideoDTO } from './DTOs';

export abstract class VideoService {
  abstract addVideo(videoId: string): Promise<VideoDTO>;

  abstract removeVideo(videoId: string): Promise<void>;

  abstract getVideoById(id: string): Promise<VideoDTO | null>;
}
