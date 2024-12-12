import {
  Args,
  ArgsType,
  Field,
  Mutation,
  ObjectType,
  ResolveField,
  Resolver,
} from '@nestjs/graphql';
import { OAuthProvider } from '../../../User';
import { IResponseModel, StringResponse } from '../Types/ResponseModel';

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
    @Field(() => StringResponse)
    exchangeAccessToken: IResponseModel<string>;
  }

  @Resolver(() => Endpoint)
  export class EndpointResolver {
    @ResolveField()
    exchangeAccessToken(
      @Args() args: exchangeAccessTokenInput,
    ): IResponseModel<string> {
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
