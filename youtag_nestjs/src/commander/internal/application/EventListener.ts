import {Injectable, Logger} from "@nestjs/common";
import {Events} from "../../../Utils/Constants";
import {TagService} from "../../../tag/api/Services";
import {VideoService} from "../../../video/api/Services";
import {eventEmitter} from "../../../Utils/EventEmitter";

@Injectable()
export class EventListener {
    constructor(private readonly tagService: TagService, private readonly videoService: VideoService) {
        //Removing unused video is a fire and forget kind of task so it can be a event listener
        eventEmitter.on(Events.REMOVE_UNUSED_VIDEOS, async (videos: string[]) => {
            this.log.debug(
                `Checking If the video is used by any other users ${videos}. If not it will be removed`,
            );
            const videosNotInUse = await this.tagService.getVideosNotInUse(videos);
            this.log.debug(
                `Removing videos ${videosNotInUse} as they are not used by any user`,
            );
            await this.videoService.removeVideos(videosNotInUse);
        }, {async: true})
    }

    private log = new Logger(EventListener.name)

}