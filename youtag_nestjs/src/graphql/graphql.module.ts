import { Module } from '@nestjs/common';

import { CommanderModule } from '../commander/commander.module';

//TODO Dynamic imports of resolver using default exports
@Module({
  providers: [],
  imports: [CommanderModule],
})
export class GraphqlModule {}
