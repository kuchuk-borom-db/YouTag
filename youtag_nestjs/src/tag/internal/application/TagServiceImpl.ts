import { DataAndTotalCount } from 'src/Utils/Models';
import { TagService } from '../../api/Services';
import { Injectable, Logger } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { TagEntity } from '../domain/Entities';
import { In, Repository } from 'typeorm';

@Injectable()
export default class TagServiceImpl extends TagService {
  constructor(
    @InjectRepository(TagEntity) private repo: Repository<TagEntity>,
  ) {
    super();
  }

  private log = new Logger(TagServiceImpl.name);

  async addTags(
    userId: string,
    videoId: string[],
    tags: string[],
  ): Promise<void> {
    this.log.debug(
      `Adding tags ${tags} to videos ${videoId} for user ${userId}`,
    );
    let tagEntities: TagEntity[] = [];
    videoId.forEach((videoId) => {
      tags.forEach((tag) => {
        const tagToAdd = new TagEntity();
        tagToAdd.tag = tag;
        tagToAdd.userId = userId;
        tagToAdd.videoId = videoId;
        tagEntities.push(tagToAdd);
      });
    });
    await this.repo.save(tagEntities);
  }

  async removeTagsFromVideos(
    userId: string,
    videoId: string[],
    tags: string[],
  ): Promise<void> {
    try {
      await this.repo.delete({
        tag: In(tags),
        videoId: In(videoId),
        userId: userId,
      });
    } catch (err) {
      this.log.error('Error while removing tags', err);
      return;
    }
    throw new Error('Method not implemented.');
  }

  async getTagsAndCountOfVideo(
    userId: string,
    videoId: string[],
    pagination?: { skip: number; limit: number },
  ): Promise<DataAndTotalCount<string> | null> {
    this.log.debug(
      `Get tags and tag counts of video ${videoId} of user ${userId}`,
    );
    if (pagination) {
      const result = await this.repo.findAndCount({
        skip: pagination.skip,
        take: pagination.limit,
        where: {
          userId: userId,
          videoId: In(videoId),
        },
      });
      return {
        datas: result[0].map((value) => value.tag),
        count: result[1],
      };
    }
    const count = await this.repo.countBy({
      videoId: In(videoId),
      userId: userId,
    });
    return {
      datas: [],
      count: count,
    };
  }

  async getVideoIdsAndCountWithTags(
    userId: string,
    tags: string[],
    pagination?: { skip: number; limit: number },
  ): Promise<DataAndTotalCount<string> | null> {
    /*
    REMINDER
    Query Strategy: Find videos with EXACTLY ALL specified tags

    Sample Data Scenario:
    VID_1 has tags: TAG_1, TAG_2, TAG_3
    VID_2 has tags: TAG_2, TAG_3, TAG_4

    Example Scenario:
    - Passed Tags: ['TAG_1', 'TAG_2', 'TAG_4']
    - Expected Output: [] (empty array)

    Query Progression Breakdown:

    1. DISTINCT Keyword:
       - Prevents duplicate video entries
       - Conceptually "groups" videos by their unique ID
       - Imagine grouping results like:
         VID_1: [TAG_1, TAG_2, TAG_3]
         VID_2: [TAG_2, TAG_3, TAG_4]
       (Note: This is a simplified mental model, not exact database behavior)

    2. IN Clause:
       - Initial broad matching
       - Returns videos with AT LEAST ONE matching tag
       - First query result would be [VID_1, VID_2]

       Detailed Matching Process:
       - VID_1 matches because it has TAG_1
       - VID_2 matches because it has TAG_4
       - Initially looks like both videos are valid

    3. Intermediate Result (Before HAVING):
       Conceptual result set:
       VID_1: [TAG_1, TAG_2]
       VID_2: [TAG_4]

    4. HAVING Clause:
       - Final filtering mechanism
       - Checks if number of unique tags EXACTLY matches input

       In this scenario:
       - VID_1 has 2 unique tags
       - Input requires 3 unique tags
       - VID_1 gets EXCLUDED

       - VID_2 has only 1 matching tag
       - VID_2 also gets EXCLUDED

    Final Result: Empty array [], because no single video has ALL specified tags

    Mental Model:
    - Think of it like a strict tag matching game
    - A video must have EVERY single tag you specify
    - Missing even one tag means the video is out
    */
  }

  getTaggedVideosOfUser(
    userId: string,
    pagination?: { skip: number; limit: number },
  ): Promise<DataAndTotalCount<string> | null> {
    throw new Error('Method not implemented.');
  }

  getTagsAndCountContaining(
    userId: string,
    containing: string,
    pagination?: { skip: number; limit: number },
  ): Promise<DataAndTotalCount<string> | null> {
    throw new Error('Method not implemented.');
  }
}
