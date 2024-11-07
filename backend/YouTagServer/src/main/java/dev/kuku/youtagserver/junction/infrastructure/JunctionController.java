package dev.kuku.youtagserver.junction.infrastructure;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authenticated/junction")
public class JunctionController {
    /*
    1. Add (tags) (videos)
        If both are present then add the videos with the tags
        If both are not present it's invalid

        If tags are present but videos are not then it's invalid
        If tags are not present but videos are present then add the videos with * tag. * Represents videos with no tag

    2. Get (tags) (videos)
        If both are present then it's invalid
        If both are not present then return all videos with tags

        If tags are present but not videos them get all videos with the tags
        If no tags are present but videos are present then get all videos mentioned with its tags

    3. Delete (tags) (videos)
        If both are present then it's invalid
        If both are not present then delete EVERYTHING

        If tags are present but videos are not then remove all videos that are using the tags.
        If tags are not present but videos are then remove all videos that are listed.

     */
}
