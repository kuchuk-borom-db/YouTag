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
    /*
    All coordinated operations between different services are handled by this module. Although this introduces tight coupling as all services are present. It's much easier to maintain.
    Compared to Event Driven design where everything is decoupled and listeners are used. It may get very hard to track the flow of the program.
    Apart from it circular event dispatching and listening is hard to debug. EDA requires careful and thoughtful design upfront with good documentation describing the flow of the program.
    We use Event dispatcher and listener for TRULY FIRE AND FORGET Operations.
     */
}
