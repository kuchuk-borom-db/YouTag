import {
  Args,
  ArgsType,
  Field,
  ObjectType,
  Query,
  ResolveField,
  Resolver,
} from '@nestjs/graphql';
import { ResponseModel } from '../Types/ResponseModel';
import { OAuthProvider } from "../../../User";

export namespace PublicQuery {
  @ArgsType()
  export class getOAuthLoginURLInput {
    @Field(() => OAuthProvider, { nullable: false })
    provider: OAuthProvider;
  }

  @ObjectType()
  export class PublicQueryEndpoint {
    @Field(() => ResponseModel<string>, { nullable: false })
    getOAuthLoginURL: ResponseModel<string>;
  }

  @Resolver(() => PublicQueryEndpoint)
  export class PublicQueryEndpointResolver {
    @ResolveField(() => ResponseModel<string>, { nullable: false })
    getOAuthLoginURL(
      @Args() provider: getOAuthLoginURLInput, // Specify the input object
    ): ResponseModel<string> {
      return {
        data: `GGEZ from ${provider.provider}`,
        success: true,
        message: 'This is msg',
      };
    }
  }
}

@Resolver()
export class QueryResolver {
  @Query(() => PublicQuery.PublicQueryEndpoint)
  async publicEndpoint(): Promise<PublicQuery.PublicQueryEndpoint> {
    return new PublicQuery.PublicQueryEndpoint();
  }
}
