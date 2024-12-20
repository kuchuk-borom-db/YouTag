import * as DataLoader from 'dataloader';
import {Injectable, Logger, Scope} from '@nestjs/common';
import {VideoDTO} from "../../../video/api/DTOs";
import {OperationCommander} from "../../../commander/api/Services";

@Injectable({scope: Scope.REQUEST})
export class DataLoaderService {
    private readonly logger = new Logger(DataLoaderService.name);

    public readonly videosByUserLoader: DataLoader<
        { userId: string; skip: number; limit: number; contains: string[] },
        VideoDTO[]
    >;

    public readonly tagsByUserLoader: DataLoader<
        { userId: string; skip: number; limit: number; contains?: string },
        string[]
    >;

    public readonly videosByTagLoader: DataLoader<
        { userId: string; tagName: string; skip: number; limit: number },
        VideoDTO[]
    >;

    public readonly tagsByVideosLoader: DataLoader<
        { userId: string; videoIds: string[]; skip: number; limit: number },
        Map<string, string[]>
    >;

    constructor(private readonly opCom: OperationCommander) {

        this.videosByUserLoader = new DataLoader(async (keys) => {
            const results = await Promise.all(
                keys.map(({userId, skip, limit, contains}) =>
                    this.opCom.getVideosOfUser(skip, limit, contains, userId)
                )
            );
            return results.map((r) => r.datas);
        });

        this.tagsByUserLoader = new DataLoader(async (keys) => {
            const results = await Promise.all(
                keys.map(({userId, skip, limit, contains}) =>
                    this.opCom.getTagsOfUser(userId, skip, limit, contains)
                )
            );
            return results.map((r) => r.datas);
        });

        this.videosByTagLoader = new DataLoader(async (keys) => {
            return await Promise.all(
                keys.map(({userId, tagName, skip, limit}) =>
                    this.opCom.getVideosWithTags(userId, limit, skip, tagName)
                )
            );
        });
        this.tagsByVideosLoader = new DataLoader(async (keys) => {
            // Since we expect all requests to be for the same user and pagination,
            // we'll use the first key's values
            const {userId, skip, limit} = keys[0];

            // Collect all videoIds from the batch
            const allVideoIds = keys.flatMap(key => key.videoIds);

            this.logger.debug(`Batch loading tags for videos: ${allVideoIds.join(', ')}`);

            try {
                const result = await this.opCom.getTagsAndCountOfVideo(
                    userId,
                    allVideoIds,
                    {skip, limit}
                );

                // Create a Map to store results for each video
                const videoTagsMap = new Map<string, string[]>();

                // Process the results and organize them by videoId
                // You'll need to modify this based on how your tagService returns the data
                allVideoIds.forEach(videoId => {
                    videoTagsMap.set(videoId, result.datas);
                });

                // Return results in the same order as the keys
                return keys.map(key => videoTagsMap);
            } catch (error) {
                this.logger.error('Error batch loading tags for videos', error);
                return keys.map(() => new Map());
            }
        });
    }

    async loadTagsForVideo(
        userId: string,
        videoId: string,
        skip: number,
        limit: number
    ): Promise<string[]> {
        const result = await this.tagsByVideosLoader.load({
            userId,
            videoIds: [videoId],
            skip,
            limit,
        });
        return result.get(videoId) || [];
    }


}