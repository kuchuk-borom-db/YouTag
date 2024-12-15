import { Module } from '@nestjs/common';
import {
  AuthQueryResolver,
  PublicQueryResolver,
  RootQueryResolver,
} from './schema/query/Root';
import {
  PublicMutationResolver,
  RootMutationResolver,
} from './schema/mutation/Root';
import { CommanderModule } from '../commander/commander.module';
import { registerEnumType } from '@nestjs/graphql';
import { OAuthProvider } from '../user/api/Enums';
import { UserResolver } from './schema/type/Base';

//TODO Dynamic imports of resolver using default exports
@Module({
  providers: [
    RootQueryResolver,
    RootMutationResolver,
    PublicQueryResolver,
    AuthQueryResolver,
    PublicMutationResolver,
    UserResolver,
  ],
  imports: [CommanderModule],
})
export class GraphqlModule {
  constructor() {
    registerEnumType(OAuthProvider, {
      name: 'OAUTH_PROVIDER',
      description: 'OAuth providers such as google, github, etc',
    });
  }
}
