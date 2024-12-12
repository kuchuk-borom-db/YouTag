import { Module } from '@nestjs/common';
import { PublicQuery, QueryResolver } from './Schemas/Queries/QueryResolver';
import { registerEnumType } from '@nestjs/graphql';
import { OAuthProvider } from '../User';
import {
  MutationResolver,
  PublicMutation,
} from './Schemas/Mutations/MutationResolver';

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
