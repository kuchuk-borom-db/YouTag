import { Args, Context, ResolveField, Resolver } from '@nestjs/graphql';
import { Tag, User, Video } from '../../graphql';
import { OperationCommander } from '../../commander/api/Services';
import { Logger } from '@nestjs/common';
import { UserDTO } from '../../user/api/DTOs';

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
