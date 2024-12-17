import { Injectable, Logger } from '@nestjs/common';
import { VideoService } from '../../../video/api/Services';
import { TagService } from '../../../tag/api/Services';
import { OperationCommander } from '../../api/Services';
import { DataAndTotalCount } from '../../../Utils/Models';
import { VideoDTO } from '../../../video/api/DTOs';

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
    const failedToAdd = await this.videoService.addVideos(videos);
    this.log.debug(`Failed to save video = ${JSON.stringify(failedToAdd)}`);
    //Filter out the videos that failed to get added
    const validVideos = videos.filter((value) => !failedToAdd.includes(value));
    this.log.debug('Videos added to database\nAdding tags to database');
    await this.tagService.addTagsToVideos(userId, validVideos, tags);
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

  async getVideosOfUser(
    skip: number,
    limit: number,
    containing: string[],
    userId: string,
  ): Promise<DataAndTotalCount<VideoDTO>> {
    this.log.debug(
      `Getting videos of user skip ${skip}, limit ${limit}, containing tags ${containing}, for user ${userId}`,
    );
    let data: DataAndTotalCount<string>;
    if (containing.length == 0) {
      this.log.debug('Getting all tagged videos of user');
      data = await this.tagService.getTaggedVideosOfUser(userId, {
        skip: skip,
        limit: limit,
      });
    } else {
      this.log.debug(
        `Getting all tagged videos of user containing ${containing}`,
      );
      data = await this.tagService.getVideoIdsAndCountWithTags(
        userId,
        containing,
        {
          skip: skip,
          limit: limit,
        },
      );
    }

    let videoInfo: DataAndTotalCount<VideoDTO>;
    const videoInfos: VideoDTO[] = await Promise.all(
      data.datas.map((value) => this.videoService.getVideoById(value)),
    );

    videoInfo = {
      datas: videoInfos,
      count: data.count,
    };

    return videoInfo;
  }

  async getTagsOfUser(
    userId: string,
    skip: number,
    limit: number,
    contains?: string,
  ): Promise<DataAndTotalCount<string>> {
    this.log.debug(
      `Get tags of user ${userId} skip ${skip} and limit ${limit} and contains ${contains}`,
    );

    let data: DataAndTotalCount<string>;

    if (!contains) {
      this.log.debug('Getting all tags of user');
      data = await this.tagService.getTagsAndCountOfUser(userId, {
        skip: skip,
        limit: limit,
      });
    } else {
      this.log.debug(`Getting tags of user containing ${contains} tags`);
      data = await this.tagService.getTagsAndCountContaining(userId, contains);
    }
    return data;
  }

  async getVideosWithTags(
    id: string,
    limit: number,
    skip: number,
    tag: string,
  ): Promise<VideoDTO[]> {
    this.log.debug(`get videos with tags for user ${id}`);
    const data = await this.tagService.getVideoIdsAndCountWithTags(id, [tag], {
      skip: skip,
      limit: limit,
    });
    return await Promise.all(
      data.datas.map(async (value) => {
        return await this.videoService.getVideoById(value);
      }),
    );
  }
}
