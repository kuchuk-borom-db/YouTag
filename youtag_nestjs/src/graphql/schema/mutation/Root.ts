import {
  Args,
  Mutation,
  ObjectType,
  ResolveField,
  Resolver,
} from '@nestjs/graphql';
import { OAuthProvider } from '../../../user/api/Enums';
import { ResponseModel, StringResponse } from '../type/Base';
import { AuthCommander } from '../../../commander/api/Services';

@ObjectType()
class PublicMutation {}

@Resolver(() => PublicMutation)
export class PublicMutationResolver {
  constructor(private readonly authCommander: AuthCommander) {}

  @ResolveField(() => StringResponse, { nullable: false })
  async exchangeOAuthToken(
    @Args('token') authToken: string,
    @Args('provider', { type: () => OAuthProvider }) provider: OAuthProvider,
  ): Promise<ResponseModel<string>> {
    const url = await this.authCommander.exchangeOAuthToken(
      authToken,
      provider,
    );
    return {
      success: true,
      data: url,
    };
  }
}

@Resolver()
export class RootMutationResolver {
  @Mutation(() => PublicMutation, { nullable: false, name: 'public' })
  getPublicMutation() {
    return {};
  }
}
