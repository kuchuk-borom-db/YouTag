import {
  Args,
  Context,
  Mutation,
  ResolveField,
  Resolver,
} from '@nestjs/graphql';
import {
  AddTagsToVideosInput,
  AuthMutation,
  NoDataResponse,
  OAUTH_PROVIDER,
  PublicMutation,
  RemoveTagsFromVideosInput,
  RemoveVideosInput,
  StringResponse,
} from '../../graphql';
import {
  AuthCommander,
  OperationCommander,
} from '../../commander/api/Services';
import { Logger, UseGuards } from '@nestjs/common';
import { AuthGuard } from '../internal/infrastructure/AuthGuard';
import { UserDTO } from '../../user/api/DTOs';

@Resolver()
export class MutationResolver {
  @Mutation(() => PublicMutation, { name: 'public' })
  publicEndpoint(): PublicMutation {
    return new PublicMutation();
  }

  @UseGuards(AuthGuard)
  @Mutation(() => AuthMutation, { name: 'auth' })
  authEndpoint(): AuthMutation {
    return new AuthMutation();
  }
}

@Resolver(() => PublicMutation)
export class PublicMutationResolver {
  constructor(private readonly authCommander: AuthCommander) {}

  private log = new Logger(PublicMutationResolver.name);

  @ResolveField(() => StringResponse)
  async exchangeOAuthTokenForAccessToken(
    @Args('token') code: string,
    @Args('provider') provider: OAUTH_PROVIDER,
  ): Promise<StringResponse> {
    try {
      this.log.debug(`Exchanging OAuth token ${code} of provider ${provider}`);
      const jwtToken = await this.authCommander.exchangeOAuthToken(
        code,
        provider,
      );
      return {
        data: jwtToken,
        success: true,
      };
    } catch (error) {
      this.log.error('Error at exchangeOAuthToken', error);
      return {
        success: false,
        message: error,
        data: null,
      };
    }
  }
}

@Resolver(() => AuthMutation)
export class AuthMutationResolver {
  constructor(private readonly opCom: OperationCommander) {}

  private log = new Logger(AuthMutationResolver.name);

  @ResolveField(() => NoDataResponse)
  async addTagsToVideos(
    @Args('input') input: AddTagsToVideosInput,
    @Context() context: any,
  ): Promise<NoDataResponse> {
    try {
      const user: UserDTO = context.req.user as UserDTO;
      this.log.debug(
        `AddTagsToVideos ${JSON.stringify(input)} for user ${user.id}`,
      );
      await this.opCom.addTagsToVideos(input.tagNames, input.videoIds, user.id);
      return {
        message: 'Added tags to videos',
        success: true,
      };
    } catch (error) {
      this.log.error('Error at addTagsToVideos', error);
      return {
        message: error,
        success: false,
      };
    }
  }

  @ResolveField(() => NoDataResponse)
  async removeTagsFromVideos(
    @Args('input') input: RemoveTagsFromVideosInput,
    @Context() context: any,
  ): Promise<NoDataResponse> {
    try {
      const user: UserDTO = context.req.user;
      this.log.debug(
        `Removing tags from videos ${JSON.stringify(input)} for user ${user.id}`,
      );
      const tags = input.tagNames;
      const videos = input.videoIds;
      await this.opCom.removeTagsFromVideos(tags, videos, user.id);
      return {
        success: true,
      };
    } catch (error) {
      this.log.error('Error at removeTagsFromVideos', error);
      return {
        success: false,
        message: error,
      };
    }
  }

  @ResolveField(() => NoDataResponse)
  async removeVideos(
    @Args('input') input: RemoveVideosInput,
    @Context() context: any,
  ): Promise<NoDataResponse> {
    try {
      const user = context.req.user as UserDTO;
      this.log.debug(
        `Removing videos ${JSON.stringify(input)} of user ${user.id}`,
      );
      await this.opCom.removeVideos(input.videoIds, user.id);
      return {
        success: true,
      };
    } catch (error) {
      this.log.error('Error at removeVideos', error);
      return {
        success: false,
        message: error,
      };
    }
  }
}
