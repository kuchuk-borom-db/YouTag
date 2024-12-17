import { Args, Context, Parent, ResolveField, Resolver } from '@nestjs/graphql';
import { Tag, User, Video } from '../../graphql';
import { OperationCommander } from '../../commander/api/Services';
import { Logger } from '@nestjs/common';
import { UserDTO } from '../../user/api/DTOs';
import { VideoDTO } from '../../video/api/DTOs';

@Resolver(() => User)
export class UserTypeResolver {
  constructor(private opCom: OperationCommander) {}

  private log = new Logger(UserTypeResolver.name);

  @ResolveField()
  async videos(
    @Args('skip')
    skip: number,
    @Args('limit')
    limit: number,
    @Args('contains')
    contains: string[],
    @Context()
    context: any,
  ): Promise<Video[]> {
    const user = context.req.user as UserDTO;
    this.log.debug(
      `Getting videos of user ${user.id} with skip ${skip} limit ${limit} and contains ${contains}`,
    );
    const data = await this.opCom.getVideosOfUser(
      skip,
      limit,
      contains,
      user.id,
    );
    return data.datas.map((value) => {
      const vid = new Video();
      vid.author = value.author;
      vid.id = value.videoId;
      vid.authorUrl = value.authorUrl;
      vid.title = value.title;
      vid.thumbnail = value.thumbnailUrl;
      return vid;
    });
  }

  @ResolveField()
  async tags(
    @Args('skip')
    skip: number,
    @Args('limit')
    limit: number,
    @Context()
    context: any,
    @Args('contains')
    contains?: string,
  ): Promise<Tag[]> {
    const user = context.req.user as UserDTO;
    this.log.debug(
      `Getting tags of user ${user.id} with skip ${skip} limit ${limit} and contains ${contains}`,
    );

    const data = await this.opCom.getTagsOfUser(user.id, skip, limit, contains);
    return data.datas.map((value) => {
      const tag = new Tag();
      tag.name = value;
      return tag;
    });
  }
}

@Resolver(() => Tag)
export class TagTypeResolver {
  constructor(private readonly opCom: OperationCommander) {}

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
      this.log.debug(
        `VideosWithTags with skip ${skip} limit ${limit} for user ${user.id}`,
      );
      const videos: VideoDTO[] = await this.opCom.getVideosWithTags(
        user.id,
        limit,
        skip,
        parent.name,
      );
      return videos.map((value) => {
        const vid = new Video();
        vid.title = value.title;
        vid.id = value.videoId;
        vid.author = value.author;
        vid.authorUrl = value.authorUrl;
        vid.thumbnail = value.thumbnailUrl;
        return vid;
      });
    } catch (error) {
      this.log.error(`Error at videos with tags ${error}`);
      return [];
    }
  }
}
