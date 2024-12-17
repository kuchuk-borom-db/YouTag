import { Args, Parent, ResolveField, Resolver } from '@nestjs/graphql';
import { Tag, User, Video } from '../../graphql';

@Resolver()
export class GenericResolver {
  private generateMockData(
    parentType: any,
    type: 'tags' | 'videos',
    args?: any,
  ): any[] {
    const items: any[] = [];
    const parentIdentifier = parentType.name || parentType.title || 'Unknown';

    for (let i = 0; i < 10; i++) {
      if (type === 'tags') {
        items.push({
          name: `TAG_${parentIdentifier} no. ${i + 1}`,
        });
      } else if (type === 'videos') {
        items.push({
          title: `Video_FROM PARENT_${parentIdentifier} no. ${i + 1}`,
          id: `Video_FROM PARENT_${parentIdentifier}`,
          author: 'TEST',
          authorUrl: 'TEST',
          thumbnail: 'TEST',
        });
      }
    }
    return items;
  }

  // Shared method to resolve tags
  protected resolveGenericTags(
    parent: User | Video | Tag,
    skip?: number,
    limit?: number,
    contains?: string,
  ): Tag[] {
    return this.generateMockData(parent, 'tags');
  }

  // Shared method to resolve videos
  protected resolveGenericVideos(
    parent: User | Tag | Video,
    skip?: number,
    limit?: number,
    contains?: string,
  ): Video[] {
    return this.generateMockData(parent, 'videos');
  }
}

// Separate resolvers for each type
@Resolver(User)
export class UserResolver extends GenericResolver {
  @ResolveField(() => [Tag])
  async tags(
    @Parent() parent: User,
    @Args('skip', { nullable: true }) skip?: number,
    @Args('limit', { nullable: true }) limit?: number,
    @Args('contains', { nullable: true }) contains?: string,
  ): Promise<Tag[]> {
    return this.resolveGenericTags(parent, skip, limit, contains);
  }

  @ResolveField(() => [Video])
  async videos(
    @Parent() parent: User,
    @Args('skip', { nullable: true }) skip?: number,
    @Args('limit', { nullable: true }) limit?: number,
    @Args('contains', { nullable: true }) contains?: string,
  ): Promise<Video[]> {
    return this.resolveGenericVideos(parent, skip, limit, contains);
  }
}

@Resolver(Tag)
export class TagResolver extends GenericResolver {
  @ResolveField(() => [Video])
  async videosWithTag(
    @Parent() parent: Tag,
    @Args('skip', { nullable: true }) skip?: number,
    @Args('limit', { nullable: true }) limit?: number,
  ): Promise<Video[]> {
    return this.resolveGenericVideos(parent, skip, limit);
  }
}

@Resolver(Video)
export class VideoResolver extends GenericResolver {
  @ResolveField(() => [Tag])
  async associatedTags(
    @Parent() parent: Video,
    @Args('skip', { nullable: true }) skip?: number,
    @Args('limit', { nullable: true }) limit?: number,
  ): Promise<Tag[]> {
    return this.resolveGenericTags(parent, skip, limit);
  }
}
