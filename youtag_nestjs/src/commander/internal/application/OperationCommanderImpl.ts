import { Injectable, Logger } from '@nestjs/common';
import { VideoService } from '../../../video/api/Services';
import { TagService } from '../../../tag/api/Services';
import { OperationCommander } from '../../api/Services';
import { DataAndTotalCount } from '../../../Utils/Models';
import { VideoDTO } from '../../../video/api/DTOs';
import { Events } from '../../../Utils/Constants';
import { eventEmitter } from '../../../Utils/EventEmitter';

@Injectable()
export class OperationCommanderImpl extends OperationCommander {
    constructor(
        private readonly tagService: TagService,
        private readonly videoService: VideoService,
    ) {
        super();
    }

    private log = new Logger(OperationCommanderImpl.name);

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

        const validVideos = videos.filter((value) => !failedToAdd.includes(value));
        this.log.debug('Videos added to database. Now, Adding tags to database');
        await this.tagService.addTagsToVideos(userId, validVideos, tags);

        await this.tagService.invalidateUserVideoCache(userId, videos);
    }

    async removeTagsFromVideos(tags: string[], videos: string[], userId: string) {
        this.log.debug(
            `Removing tags ${tags} from videos ${videos} of user ${userId}`,
        );
        await this.tagService.removeTagsFromVideos(userId, videos, tags);
        eventEmitter.emit(Events.REMOVE_UNUSED_VIDEOS, videos);
        await this.tagService.invalidateUserVideoCache(userId, videos);
    }

    async removeVideos(videoIds: string[], userId: string): Promise<void> {
        this.log.debug(`Removing videos ${videoIds} from user ${userId}`);
        await this.tagService.removeAllTagsFromVideos(userId, videoIds);
        eventEmitter.emit(Events.REMOVE_UNUSED_VIDEOS, videoIds);
        await this.tagService.invalidateUserVideoCache(userId, videoIds);
        await this.videoService.invalidateVideosCache(videoIds);
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
        if (containing.length === 0) {
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

        const videoInfos: VideoDTO[] = await this.videoService.getVideosByIds(data.datas);

        const sortedVideos = videoInfos.sort((a, b) =>
            a.title.localeCompare(b.title),
        );

        return {
            datas: sortedVideos,
            count: data.count,
        };
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

        const sortedTags = data.datas.sort((a, b) => a.localeCompare(b));

        return {
            datas: sortedTags,
            count: data.count,
        };
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

        const sortedVideos = videos.sort((a, b) =>
            a.title.localeCompare(b.title),
        );

        return {
            count: data.count,
            datas: sortedVideos,
        };
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
}
