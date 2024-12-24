import {Injectable, Logger} from "@nestjs/common";
import {OnEvent} from "@nestjs/event-emitter";
import {Events} from "../../../Utils/Constants";
import {TagService} from "../../../tag/api/Services";
import {VideoService} from "../../../video/api/Services";

@Injectable()
export class EventListener {
    constructor(private readonly tagService: TagService, private readonly videoService: VideoService) {
    }

    private log = new Logger(EventListener.name)

    @OnEvent(Events.REMOVE_UNUSED_VIDEOS, {async: true})
    async handleRemoveUnusedVideos(videos: string[]) {
        this.log.debug(
            `Checking If the video is used by any other users ${videos}. If not it will be removed`,
        );
        const videosNotInUse = await this.tagService.getVideosNotInUse(videos);
        this.log.debug(
            `Removing videos ${videosNotInUse} as they are not used by any user`,
        );
        await this.videoService.removeVideos(videosNotInUse);
    }
}