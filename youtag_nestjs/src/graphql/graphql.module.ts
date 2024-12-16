import { Module } from '@nestjs/common';

import { CommanderModule } from '../commander/commander.module';
import {
  AuthQueryResolver,
  PublicQueryResolver,
  QueryResolver,
} from './resolvers/QueryResolver';
import {
  TagResolver,
  UserResolver,
  VideoResolver,
} from './resolvers/TypeResolver';

//TODO Dynamic imports of resolver using default exports
@Module({
  providers: [
    QueryResolver,
    PublicQueryResolver,
    AuthQueryResolver,
    TagResolver,UserResolver,VideoResolver
  ],
  imports: [CommanderModule],
})
export class GraphqlModule {}
