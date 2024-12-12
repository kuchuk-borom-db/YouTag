import { Module } from '@nestjs/common';
import { PublicQuery, QueryResolver } from './Schemas/Queries/QueryResolver';
import { registerEnumType } from '@nestjs/graphql';
import {
  MutationResolver,
  PublicMutation,
} from './Schemas/Mutations/MutationResolver';
import { API } from "../User.module/User.module";
import OAuthProvider = API.OAuthProvider;

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
