import { Module } from '@nestjs/common';
import { PublicQuery, QueryResolver } from './schemas/QueryResolver';
import { registerEnumType } from '@nestjs/graphql';
import { OAuthProvider } from '../user/enums';
import { MutationResolver, PublicMutation } from './schemas/MutationResolver';

@Module({
  providers: [
    QueryResolver,
    PublicQuery.PublicQueryEndpointResolver,
    MutationResolver,
    PublicMutation.EndpointResolver,
  ],
})
export class GraphqlModule {
  constructor() {
    registerEnumType(OAuthProvider, {
      name: 'OAUTH_PROVIDER',
      description: 'OAuth Providers enum',
    });
  }
}
