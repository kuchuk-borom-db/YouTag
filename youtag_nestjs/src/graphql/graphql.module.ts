import {Module} from '@nestjs/common';

import {CommanderModule} from '../commander/commander.module';
import {AuthQueryResolver, PublicQueryResolver, QueryResolver,} from './resolvers/QueryResolver';
import {TagTypeResolver, UserTypeResolver, VideoTypeResolver,} from './resolvers/TypeResolver';
import {AuthMutationResolver, MutationResolver, PublicMutationResolver,} from './resolvers/MutationResolver';

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
    ],
    imports: [CommanderModule],
})
export class GraphqlModule {
}

//TODO Optimise data loader service. It is not working for tag resolver as of now