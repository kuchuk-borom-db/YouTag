import { DataAndTotalCount } from '../../Utils/Models';

export abstract class TagService {
  /**
   * Add tags to videos and total count
   * @param userId
   * @param videoId
   * @param tags
   */
  abstract addTags(
    userId: string,
    videoId: string[],
    tags: string[],
  ): Promise<void>;

  /**
   * remove tags from the videos and total count
   * @param userId
   * @param videoId
   * @param tags
   */
  abstract removeTagsFromVideos(
    userId: string,
    videoId: string[],
    tags: string[],
  ): Promise<void>;

  /**
   * Get combined tags of the videos and total count
   * @param userId
   * @param videoId
   * @param pagination
   */
  abstract getTagsAndCountOfVideo(
    userId: string,
    videoId: string[],
    pagination?: {
      skip: number;
      limit: number;
    },
  ): Promise<DataAndTotalCount<string> | null>;

  /**
   * get tags of user and total count
   * @param userId
   * @param pagination
   */
  abstract getTagsAndCountOfUser(
    userId: string,
    pagination?: {
      skip: number;
      limit: number;
    },
  ): Promise<DataAndTotalCount<string> | null>;

  /**
   * Get videos with the tags and total count
   * @param userId
   * @param tags
   * @param pagination
   */
  abstract getVideoIdsAndCountWithTags(
    userId: string,
    tags: string[],
    pagination?: {
      skip: number;
      limit: number;
    },
  ): Promise<DataAndTotalCount<string> | null>;

  /**
   * Get all tagged videos of user and total count
   * @param userId
   * @param pagination
   */
  abstract getTaggedVideosOfUser(
    userId: string,
    pagination?: {
      skip: number;
      limit: number;
    },
  ): Promise<DataAndTotalCount<string> | null>;

  /**
   * Get tags containing keyword and total count
   * @param userId
   * @param containing
   * @param pagination
   */
  abstract getTagsAndCountContaining(
    userId: string,
    containing: string,
    pagination?: {
      skip: number;
      limit: number;
    },
  ): Promise<DataAndTotalCount<string> | null>;
}
