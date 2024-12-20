import {Args, Context, Parent, ResolveField, Resolver} from '@nestjs/graphql';
import {Tag, User, Video} from '../../graphql';
import {Logger} from '@nestjs/common';
import {UserDTO} from '../../user/api/DTOs';
import {DataLoaderService} from "../internal/application/DataLoaderService";

@Resolver(() => User)
export class UserTypeResolver {
    constructor(private dataLoader: DataLoaderService) {
    }

    private log = new Logger(UserTypeResolver.name);

    @ResolveField()
    async videos(
        @Args('skip') skip: number,
        @Args('limit') limit: number,
        @Args('contains') contains: string[],
        @Context() context: any,
    ): Promise<Video[]> {
        const user = context.req.user as UserDTO;
        const videos = await this.dataLoader.videosByUserLoader.load({
            userId: user.id,
            skip,
            limit,
            contains,
        });

        return videos.map((value) => ({
            author: value.author,
            id: value.videoId,
            authorUrl: value.authorUrl,
            title: value.title,
            thumbnail: value.thumbnailUrl,
        }));
    }

    @ResolveField()
    async tags(
        @Args('skip') skip: number,
        @Args('limit') limit: number,
        @Context() context: any,
        @Args('contains') contains?: string,
    ): Promise<Tag[]> {
        const user = context.req.user as UserDTO;
        const tags = await this.dataLoader.tagsByUserLoader.load({
            userId: user.id,
            skip,
            limit,
            contains,
        });

        return tags.map((value) => ({name: value}));
    }
}

@Resolver(() => Tag)
export class TagTypeResolver {
    constructor(private readonly dataLoader: DataLoaderService) {
    }

    private log = new Logger(TagTypeResolver.name);

    @ResolveField()
    async videosWithTag(
        @Context() context: any,
        @Args('skip') skip: number,
        @Args('limit') limit: number,
        @Parent() parent: Tag,
    ): Promise<Video[]> {
        try {
            const user = context.req.user as UserDTO;
            const videos = await this.dataLoader.videosByTagLoader.load({
                userId: user.id,
                tagName: parent.name,
                skip,
                limit,
            });

            return videos.map((value) => ({
                title: value.title,
                id: value.videoId,
                author: value.author,
                authorUrl: value.authorUrl,
                thumbnail: value.thumbnailUrl,
            }));
        } catch (error) {
            this.log.error(`Error at videos with tags ${error}`);
            return [];
        }
    }
}

@Resolver(() => Video)
export class VideoTypeResolver {
    constructor(private readonly dataLoader: DataLoaderService) {
    }

    private log = new Logger(VideoTypeResolver.name);

    @ResolveField()
    async associatedTags(
        @Context() context: any,
        @Args('skip') skip: number,
        @Args('limit') limit: number,
        @Parent() parent: Video,
    ): Promise<Tag[]> {
        const user = context.req.user as UserDTO;
        const tags = await this.dataLoader.tagsByVideoLoader.load({
            userId: user.id,
            videoId: parent.id,
            skip,
            limit,
        });

        return tags.map((value) => ({name: value}));
    }
}