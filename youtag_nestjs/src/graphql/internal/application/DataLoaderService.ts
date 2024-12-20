import * as DataLoader from 'dataloader';
import { Injectable, Scope } from '@nestjs/common';
import {VideoDTO} from "../../../video/api/DTOs";
import {OperationCommander} from "../../../commander/api/Services";

@Injectable({ scope: Scope.REQUEST })
export class DataLoaderService {
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

    public readonly tagsByVideoLoader: DataLoader<
        { userId: string; videoId: string; skip: number; limit: number },
        string[]
    >;

    constructor(private readonly opCom: OperationCommander) {
        this.videosByUserLoader = new DataLoader(async (keys) => {
            const results = await Promise.all(
                keys.map(({ userId, skip, limit, contains }) =>
                    this.opCom.getVideosOfUser(skip, limit, contains, userId)
                )
            );
            return results.map((r) => r.datas);
        });

        this.tagsByUserLoader = new DataLoader(async (keys) => {
            const results = await Promise.all(
                keys.map(({ userId, skip, limit, contains }) =>
                    this.opCom.getTagsOfUser(userId, skip, limit, contains)
                )
            );
            return results.map((r) => r.datas);
        });

        this.videosByTagLoader = new DataLoader(async (keys) => {
            const results = await Promise.all(
                keys.map(({ userId, tagName, skip, limit }) =>
                    this.opCom.getVideosWithTags(userId, limit, skip, tagName)
                )
            );
            return results;
        });

        this.tagsByVideoLoader = new DataLoader(async (keys) => {
            const results = await Promise.all(
                keys.map(({ userId, videoId, skip, limit }) =>
                    this.opCom.getTagsOfVideo(userId, skip, limit, videoId)
                )
            );
            return results.map((r) => (r ? r.datas : []));
        });
    }
}