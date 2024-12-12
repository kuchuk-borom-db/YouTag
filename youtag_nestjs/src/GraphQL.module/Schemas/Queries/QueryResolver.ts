import {
  Args,
  ArgsType,
  Field,
  ObjectType,
  Query,
  ResolveField,
  Resolver,
} from '@nestjs/graphql';
import { OAuthProvider } from '../../../User';
import { IResponseModel, StringResponse } from '../Types/ResponseModel';
import GoogleAuthServiceImpl from '../../../User.module/application/GoogleAuthServiceImpl';

export namespace PublicQuery {
  @ArgsType()
  export class getOAuthLoginURLInput {
    a = GoogleAuthServiceImpl;
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
