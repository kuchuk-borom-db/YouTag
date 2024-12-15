import {
  Args,
  ObjectType,
  Query,
  ResolveField,
  Resolver,
} from '@nestjs/graphql';
import { OAuthProvider } from '../../../user/api/Enums';
import {
  GQL_Type_User,
  ResponseModel,
  ResponseModelResolver,
  StringResponse,
} from '../type/Base';
import { AuthCommander } from '../../../commander/api/Services';

@ObjectType()
class PublicQuery {}

@Resolver(() => PublicQuery)
export class PublicQueryResolver {
  constructor(private readonly authCommander: AuthCommander) {}

  @ResolveField(() => StringResponse, { nullable: false })
  async getOAuthLoginURL(
    @Args('provider', { type: () => OAuthProvider }) provider: OAuthProvider,
  ): Promise<ResponseModel<string>> {
    const url = await this.authCommander.getOAuthLoginURL(provider);
    return {
      success: true,
      data: url,
    };
  }
}

@ObjectType()
class AuthQuery {}

const userResolver = ResponseModelResolver(GQL_Type_User);

@Resolver(() => AuthQuery)
export class AuthQueryResolver {
  @ResolveField(() => userResolver, { nullable: false })
  getUserInfo(): ResponseModel<GQL_Type_User> {
    return {
      success: true,
      data: {},
    };
  }
}

@Resolver()
export class RootQueryResolver {
  @Query(() => PublicQuery, { name: 'public', nullable: false })
  publicEndpoint(): PublicQuery {
    return {};
  }

  @Query(() => AuthQuery, { name: 'auth', nullable: false })
  authEndpoint(): AuthQuery {
    return {};
  }
}
