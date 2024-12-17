import { Args, Mutation, ResolveField, Resolver } from '@nestjs/graphql';
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
  TagCommander,
  VideoCommander,
} from '../../commander/api/Services';
import { Logger, UseGuards } from '@nestjs/common';
import { AuthGuard } from '../internal/infrastructure/AuthGuard';

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
  constructor(
    private readonly tagVideoCommander: TagCommander,
    videoCommander: VideoCommander,
  ) {}

  private log = new Logger(AuthMutationResolver.name);

  @ResolveField(() => NoDataResponse)
  async addTagsToVideos(
    @Args('input') input: AddTagsToVideosInput,
  ): Promise<NoDataResponse> {
    this.log.debug(`AddTagsToVideos ${JSON.stringify(input)}`);
    return {
      message: 'Added tags to videos',
      success: true,
    };
  }

  @ResolveField(() => NoDataResponse)
  async removeTagsFromVideos(
    @Args() input: RemoveTagsFromVideosInput,
  ): Promise<NoDataResponse> {
    return {
      message: 'WIP',
      success: true,
    };
  }

  @ResolveField(() => NoDataResponse)
  async removeVideos(
    @Args() input: RemoveVideosInput,
  ): Promise<NoDataResponse> {
    return {
      message: 'WIP',
      success: true,
    };
  }
}
