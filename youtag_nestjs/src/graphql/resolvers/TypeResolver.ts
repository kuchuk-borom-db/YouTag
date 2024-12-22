import {Args, Context, Parent, ResolveField, Resolver} from '@nestjs/graphql';
import {Tag, TagsResponse, User, Video, VideosResponse} from '../../graphql';
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
    ): Promise<VideosResponse> {
        try {
            const user = context.req.user as UserDTO;
            this.log.debug(`Getting videos of user ${user.id}`)
            const videos = await this.dataLoader.videosByUserLoader.load({
                userId: user.id,
                skip,
                limit,
                contains,
            });

            return {
                count: videos.count,
                data: videos.datas.map(value => ({
                    author: value.author,
                    id: value.videoId,
                    authorUrl: value.authorUrl,
                    title: value.title,
                    thumbnail: value.thumbnailUrl
                })),
                success: true
            }
        } catch (err) {
            this.log.error(`Error at getting videos of user ${err}`)
            return {
                success: false,
                message: err,
                count: 0,
                data: []
            }
        }
    }

    @ResolveField()
    async tags(
        @Args('skip') skip: number,
        @Args('limit') limit: number,
        @Context() context: any,
        @Args('contains') contains?: string,
    ): Promise<TagsResponse> {
        try {
            const user = context.req.user as UserDTO;
            const tags = await this.dataLoader.tagsByUserLoader.load({
                userId: user.id,
                skip,
                limit,
                contains,
            });

            return {
                count: tags.count, data: tags.datas.map(tag => ({name: tag})), success: true
            }
        } catch (err) {
            this.log.error(`Error at getting tags for user ${err}`)
            return {
                success: false, data: [], message: err, count: 0
            }
        }
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
    ): Promise<VideosResponse> {
        try {
            const user = context.req.user as UserDTO;
            const videos = await this.dataLoader.videosByTagLoader.load({
                userId: user.id,
                tagName: parent.name,
                skip,
                limit,
            });

            const videosMap = videos.datas.map((value) => ({
                title: value.title,
                id: value.videoId,
                author: value.author,
                authorUrl: value.authorUrl,
                thumbnail: value.thumbnailUrl,
            }));
            return {
                count: videos.count,
                data: videosMap,
                success: true
            }
        } catch (error) {
            this.log.error(`Error at videos with tags ${error}`);
            return {
                count: 0,
                success: false,
                data: [],
                message: error
            }
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
    ): Promise<TagsResponse> {
        try {
            this.log.debug(`Get associated tags for video ${parent.id}`)
            const user = context.req.user as UserDTO;
            const tags = await this.dataLoader.loadTagsForVideo(
                user.id,
                parent.id,
                skip,
                limit
            );
            return {
                count: tags.count,
                data: tags.datas.map(value => {
                    return {name: value}
                }),
                success: true
            }
        } catch (err) {
            this.log.error(`Error at associate tags ${err}`)
            return {
                success: false,
                message: err,
                count: 0,
                data: []
            }
        }
    }
}