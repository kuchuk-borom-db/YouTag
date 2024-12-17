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
