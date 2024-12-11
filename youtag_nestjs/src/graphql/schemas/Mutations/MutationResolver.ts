import {
  Args,
  ArgsType,
  Field,
  Mutation,
  ObjectType,
  ResolveField,
  Resolver,
} from '@nestjs/graphql';
import { ResponseModel } from '../Types/ResponseModel';
import { OAuthProvider } from "../../../user";

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
    exchangeAccessToken: ResponseModel<string>;
  }

  @Resolver(() => Endpoint)
  export class EndpointResolver {
    @ResolveField()
    exchangeAccessToken(
      @Args() args: exchangeAccessTokenInput,
    ): ResponseModel<string> {
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
