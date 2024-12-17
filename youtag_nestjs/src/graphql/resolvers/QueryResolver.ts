import { Args, Context, Query, ResolveField, Resolver } from '@nestjs/graphql';
import {
  AuthQuery,
  OAUTH_PROVIDER,
  PublicQuery,
  StringResponse,
  UserResponse,
} from '../../graphql';
import { AuthCommander } from '../../commander/api/Services';
import { UseGuards } from '@nestjs/common';
import { AuthGuard } from '../internal/infrastructure/AuthGuard';
import { UserDTO } from '../../user/api/DTOs';

@Resolver()
export class QueryResolver {
  @Query(() => PublicQuery)
  publicData(): PublicQuery | Promise<PublicQuery> {
    return new PublicQuery();
  }

  @UseGuards(AuthGuard)
  @Query(() => AuthQuery, { nullable: false })
  authenticatedData(): AuthQuery | Promise<AuthQuery> {
    return new AuthQuery();
  }
}

@Resolver(() => PublicQuery)
export class PublicQueryResolver {
  constructor(private readonly authCommander: AuthCommander) {}

  @ResolveField(() => StringResponse)
  async getOAuthLoginURL(
    @Args() provider: OAUTH_PROVIDER,
  ): Promise<StringResponse> {
    const url = await this.authCommander.getOAuthLoginURL(provider);
    if (url)
      return {
        data: url,
        success: true,
      };
    return {
      success: false,
      message: `Failed to get OAuth login url for ${provider}`,
      data: null,
    };
  }
}

@Resolver(() => AuthQuery)
export class AuthQueryResolver {
  constructor(private readonly authCommander: AuthCommander) {}

  @ResolveField(() => UserResponse)
  async user(@Context() context: any): Promise<UserResponse> {
    const user = context.req.user as UserDTO;
    return {
      success: true,
      data: {
        name: user.name,
        email: user.id,
        thumbnail: user.thumbnailUrl,
        // Non-scalar field types such as tags,videos are resolved using resolver unlike scalar types
      },
    };
  }
}
