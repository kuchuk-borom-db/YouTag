import {
  Args,
  ArgsType,
  Field,
  Mutation,
  ObjectType,
  ResolveField,
  Resolver,
} from '@nestjs/graphql';
import { GraphQlResp } from './RootTypes';
import { OAuthProvider } from '../../user/enums';

export namespace PublicMutation {
  @ArgsType()
  class exchangeAccessTokenInput {
    @Field()
    code: string;
    @Field(() => OAuthProvider)
    provider: OAuthProvider;
  }

  @ObjectType()
  export class Endpoint {
    @Field()
    exchangeAccessToken: GraphQlResp<string>;
  }

  @Resolver(() => Endpoint)
  export class EndpointResolver {
    @ResolveField()
    exchangeAccessToken(
      @Args() args: exchangeAccessTokenInput,
    ): GraphQlResp<string> {
      return {
        success: true,
        data: `GENERATED ${args}`,
        message: ' GGEZ',
      };
    }
  }
}

@Resolver()
export class MutationResolver {
  @Mutation(() => PublicMutation.Endpoint)
  publicEndpoint(): PublicMutation.Endpoint {
    return new PublicMutation.Endpoint();
  }
}
