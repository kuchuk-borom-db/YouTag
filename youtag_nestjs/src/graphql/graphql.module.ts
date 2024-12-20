import {Module} from '@nestjs/common';

import {CommanderModule} from '../commander/commander.module';
import {AuthQueryResolver, PublicQueryResolver, QueryResolver,} from './resolvers/QueryResolver';
import {TagTypeResolver, UserTypeResolver, VideoTypeResolver,} from './resolvers/TypeResolver';
import {AuthMutationResolver, MutationResolver, PublicMutationResolver,} from './resolvers/MutationResolver';
import {DataLoaderService} from "./internal/application/DataLoaderService";

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
        TagTypeResolver,
        VideoTypeResolver,
        DataLoaderService
    ],
    imports: [CommanderModule],
})
export class GraphqlModule {
}

//TODO Use Type responses with count value