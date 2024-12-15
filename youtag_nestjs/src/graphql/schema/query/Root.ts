import {
  Args,
  ObjectType,
  Query,
  ResolveField,
  Resolver,
} from '@nestjs/graphql';
import { OAuthProvider } from '../../../user/api/Enums';
import { ResponseModel, StringResponse } from '../type/Base';
import { AuthCommander } from '../../../commander/api/Services';

@ObjectType()
class PublicQuery {}

@Resolver(() => PublicQuery)
export class PublicQueryResolver {
  constructor(private readonly authCommander: AuthCommander) {}

  @ResolveField(() => StringResponse, { nullable: false })
  async getOAuthLoginURL(
    provider: OAuthProvider,
  ): Promise<ResponseModel<string>> {
    const url = await this.authCommander.getOAuthLoginURL(provider);
    return {
      success: true,
      data: url,
    };
  }
}

@ObjectType()
class AuthenticatedQuery {}

@Resolver(() => AuthenticatedQuery)
export class AuthenticatedQueryResolver {
  constructor(private readonly authCommander: AuthCommander) {}

  //TODO move to public mutation
  @ResolveField(() => StringResponse, { nullable: false })
  async exchangeOAuthToken(
    @Args('authToken') authToken: string,
    @Args('provider') provider: OAuthProvider,
  ): Promise<ResponseModel<string>> {
    const token = await this.authCommander.exchangeOAuthToken(
      authToken,
      provider,
    );
    return {
      success: true,
      data: token,
    };
  }
}

@Resolver()
export class RootQueryResolver {
  @Query(() => PublicQuery, { name: 'public', nullable: false })
  publicEndpoint(): PublicQuery {
    return {};
  }
}
