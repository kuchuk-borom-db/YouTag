import {Module} from '@nestjs/common';
import {AuthCommander, OperationCommander} from './api/Services';
import {AuthCommanderImpl} from './internal/application/AuthCommanderImpl';
import {UserModule} from '../user/user.module';
import {OperationCommanderImpl} from './internal/application/OperationCommanderImpl';
import {TagModule} from '../tag/tag.module';
import {VideoModule} from '../video/video.module';
import {EventListener} from "./internal/application/EventListener";

@Module({
    exports: [AuthCommander, OperationCommander],
    imports: [UserModule, TagModule, VideoModule,],
    providers: [
        {
            provide: AuthCommander,
            useClass: AuthCommanderImpl,
        },

        {
            provide: OperationCommander,
            useClass: OperationCommanderImpl,
        },
        EventListener
    ],
})
export class CommanderModule {
}
