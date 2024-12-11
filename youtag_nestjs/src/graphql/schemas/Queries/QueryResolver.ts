import {
  Args,
  ArgsType,
  Field,
  ObjectType,
  Query,
  ResolveField,
  Resolver,
} from '@nestjs/graphql';
import { GraphQlResp } from '../Types/RootTypes';
import { OAuthProvider } from "../../../user";

export namespace PublicQuery {
  @ArgsType()
  export class getOAuthLoginURLInput {
    @Field(() => OAuthProvider, { nullable: false })
    provider: OAuthProvider;
  }

  @ObjectType()
  export class PublicQueryEndpoint {
    @Field(() => GraphQlResp<string>, { nullable: false })
    getOAuthLoginURL: GraphQlResp<string>;
  }

  @Resolver(() => PublicQueryEndpoint)
  export class PublicQueryEndpointResolver {
    @ResolveField(() => GraphQlResp<string>, { nullable: false })
    getOAuthLoginURL(
      @Args() provider: getOAuthLoginURLInput, // Specify the input object
    ): GraphQlResp<string> {
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
