import { Injectable, Logger } from '@nestjs/common';
import { VideoService } from '../../../video/api/Services';
import { TagService } from '../../../tag/api/Services';
import { OperationCommander } from '../../api/Services';

@Injectable()
export class OperationCommanderImpl extends OperationCommander {
  constructor(
    private readonly tagService: TagService,
    private readonly videoService: VideoService,
  ) {
    super();
  }

  private log = new Logger(OperationCommanderImpl.name);

  /**
   * Adds tags to videos. If video has not been saved in database yet, it will be saved first.
   * @param tags tags to add
   * @param videos videos to add the tags to
   * @param userId userID
   */
  async addTagsToVideos(
    tags: string[],
    videos: string[],
    userId: string,
  ): Promise<void> {
    this.log.debug(
      `Adding tags ${tags} to videos ${videos} for user ${userId}`,
    );
    this.log.debug('Adding videos to database');
    await this.videoService.addVideos(videos);
    this.log.debug('Videos added to database\nAdding tags to database');
    await this.tagService.addTagsToVideos(userId, videos, tags);
  }

  /**
   * Removes tags from videos. If all tags are removed from video. The video is completely removed from database.
   */
  async removeTagsFromVideos(tags: string[], videos: string[], userId: string) {
    this.log.debug(
      `Removing tags ${tags} from videos ${videos} of user ${userId}`,
    );
    await this.tagService.removeTagsFromVideos(userId, videos, tags);
    //TODO Event driven Approach
    this.log.debug(
      'Removed tags from videos.\nChecking If the video is used by any other users. If not it will be removed',
    );
    const videosNotInUse = await this.tagService.getVideosNotInUse(videos);
    this.log.debug(
      `Removing videos ${videos} as they are not used by any user`,
    );
    await this.videoService.removeVideos(videosNotInUse);
  }

  /**
   * Removes video and tag entries from the video. If the video is not used by any other users too. Its removed completely
   * @param videoIds
   * @param userId
   */
  async removeVideos(videoIds: string[], userId: string): Promise<void> {
    this.log.debug(`Removing videos ${videoIds} from user ${userId}`);
    await this.tagService.removeAllTagsFromVideos(userId, videoIds);
    this.log.debug(
      'Checking if removed videos are used by any user. If not then they are going to be removed',
    );
    const videosNotInUse = await this.tagService.getVideosNotInUse(videoIds);
    this.log.debug(
      `Removing videos ${videoIds} as they are not used by any user`,
    );
    await this.videoService.removeVideos(videosNotInUse);
  }
}
