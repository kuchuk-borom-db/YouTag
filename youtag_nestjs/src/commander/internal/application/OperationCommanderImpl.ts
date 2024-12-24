import {Injectable, Logger} from '@nestjs/common';
import {VideoService} from '../../../video/api/Services';
import {TagService} from '../../../tag/api/Services';
import {OperationCommander} from '../../api/Services';
import {DataAndTotalCount} from '../../../Utils/Models';
import {VideoDTO} from '../../../video/api/DTOs';
import {EventEmitter2} from "@nestjs/event-emitter";
import {Events} from "../../../Utils/Constants";

@Injectable()
export class OperationCommanderImpl extends OperationCommander {
    constructor(
        private readonly tagService: TagService,
        private readonly videoService: VideoService,
        private event: EventEmitter2
    ) {
        super();
    }

    private log = new Logger(OperationCommanderImpl.name);

    /**
     * Adds tags to videos. If video has not been saved in database yet, it will be saved first. Clears caches for all get operations
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
        this.log.debug('Videos added to database. Now, Adding tags to database');
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
        this.event.emit(Events.REMOVE_UNUSED_VIDEOS, videos)

    }

    /**
     * Removes video and tag entries from the video. If the video is not used by any other users too. Its removed completely
     * @param videoIds
     * @param userId
     */
    async removeVideos(videoIds: string[], userId: string): Promise<void> {
        this.log.debug(`Removing videos ${videoIds} from user ${userId}`);
        await this.tagService.removeAllTagsFromVideos(userId, videoIds);
        this.event.emit(Events.REMOVE_UNUSED_VIDEOS, videoIds)
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
        const videoInfos: VideoDTO[] = await this.videoService.getVideosByIds(data.datas);


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
    ): Promise<DataAndTotalCount<VideoDTO>> {
        this.log.debug(`get videos with tags for user ${id}`);
        const data = await this.tagService.getVideoIdsAndCountWithTags(id, [tag], {
            skip: skip,
            limit: limit,
        });
        const videos = await Promise.all(
            data.datas.map(async (value) => {
                return await this.videoService.getVideoById(value);
            }),
        );
        return {
            count: data.count,
            datas: videos
        }
    }

    async getTagsOfVideo(
        userId: string,
        skip: number,
        limit: number,
        videoId: string,
    ): Promise<DataAndTotalCount<string>> {
        this.log.debug(`Get tags of video ${videoId} of user ${userId}`);
        try {
            return await this.tagService.getTagsAndCountOfVideo(userId, [videoId], {
                skip: skip,
                limit: limit,
            });
        } catch (err) {
            this.log.error('Error at getTagsOfVideo', err);
            return null;
        }
    }

    async getTagsAndCountOfVideo(
        userId: string,
        videoIds: string[],
        options: { skip: number; limit: number }
    ): Promise<DataAndTotalCount<string>> {
        this.log.debug(`Get tags for videos ${videoIds.join(', ')} of user ${userId}`);
        try {
            return await this.tagService.getTagsAndCountOfVideo(userId, videoIds, options);
        } catch (err) {
            this.log.error('Error at getTagsOfVideo', err);
            return null;
        }
    }

    async getVideosWithMultipleTags(
        userId: string,
        tagNames: string[],
        limit: number,
        skip: number
    ): Promise<VideoDTO[]> {
        this.log.debug(`Getting videos with tags [${tagNames.join(', ')}] for user ${userId}`);
        try {
            //returns video Ids of videos that contains all the tags
            const videoIds: DataAndTotalCount<string> = await this.tagService.getVideoIdsAndCountWithTags(userId, tagNames, {
                skip: skip,
                limit: limit
            })
            return await this.videoService.getVideosByIds(videoIds.datas);
        } catch (error) {
            this.log.error('Error getting videos with multiple tags', error);
            return []
        }
    }

}

