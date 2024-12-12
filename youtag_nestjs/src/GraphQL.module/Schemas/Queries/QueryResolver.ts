import {
  Args,
  ArgsType,
  Field,
  ObjectType,
  Query,
  ResolveField,
  Resolver,
} from '@nestjs/graphql';
import { IResponseModel, StringResponse } from '../Types/ResponseModel';
import { API } from "../../../User.module/User.module";

export namespace PublicQuery {
  import OAuthProvider = API.OAuthProvider;

  @ArgsType()
  export class getOAuthLoginURLInput {
    @Field(() => OAuthProvider, { nullable: false })
    provider: OAuthProvider;
  }

  @ObjectType()
  export class PublicQueryEndpoint {
    @Field(() => StringResponse, { nullable: false })
    getOAuthLoginURL: IResponseModel<string>;
  }

  @Resolver(() => PublicQueryEndpoint)
  export class PublicQueryEndpointResolver {
    @ResolveField()
    getOAuthLoginURL(
      @Args() provider: getOAuthLoginURLInput, // Specify the input object
    ): IResponseModel<string> {
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
