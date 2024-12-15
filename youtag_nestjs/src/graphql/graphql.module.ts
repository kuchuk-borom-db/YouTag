import { Module } from '@nestjs/common';
import {
  AuthenticatedQueryResolver,
  PublicQueryResolver,
  RootQueryResolver,
} from './schema/query/Root';

//TODO Dynamic imports of resolver using default exports
@Module({
  providers: [
    RootQueryResolver,
    PublicQueryResolver,
    AuthenticatedQueryResolver,
  ],
})
export class GraphqlModule {}
