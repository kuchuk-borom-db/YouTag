import { Module } from '@nestjs/common';

import { CommanderModule } from '../commander/commander.module';
import {
  AuthQueryResolver,
  PublicQueryResolver,
  QueryResolver,
} from './resolvers/QueryResolver';
import { UserTypeResolver } from './resolvers/TypeResolver';
import {
  AuthMutationResolver,
  MutationResolver,
  PublicMutationResolver,
} from './resolvers/MutationResolver';

//TODO Dynamic imports of resolver using default exports
@Module({
  providers: [
    QueryResolver,
    PublicQueryResolver,
    AuthQueryResolver,
    MutationResolver,
    PublicMutationResolver,
    AuthMutationResolver,
    UserTypeResolver,
  ],
  imports: [CommanderModule],
})
export class GraphqlModule {}
