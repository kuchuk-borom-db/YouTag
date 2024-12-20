package dev.kuku.youtagserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class YouTagServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(YouTagServerApplication.class, args);
    }

}
//TODO Read Google client.json for secret stuff
//TODO Read config file for web scrapping
//TODO Replace web scraping with api key call. Add api key option in future
//TODO CORS stuff for production
//TODO Environment stuff for production
//TODO Rate limiting? IDK Yet
/*
 *
 * Spring modulith is awesome!
 * It allows you to build modular monolith application with few inter module dependencies
 * The next best thing is it's persistent event system that allows you to handle event driven application without having to run a separate messaging server
 */

/*
Operations

Get videos of a user
Get video of a user by Id if linked
Get tags of a user
Get videos of a user with tag

unlink/link a video
delete tag from video
delete tag from all videos
delete video containing specific tag NOT REQUIRED THOUGH
 */

/*
Endpoints:-

1. link video
2. unlink video
3. Add tags to video
4. Remove tags from video
5. Get video info

1. Get all videos of current user
2. Get all videos with tags T1,T2,T3 of user
 */

/*
 * Operations we are going to have
 * 1. Get videos of user (user_video table)
 * 2. Get video info with tags (video table for info, user_video_tag table for tags)
 * 3. Get videos using tag (user_video_tag table)
 * 4. Get tags containing certain word (tags table)
 * 5. Get tags of user (tags table)
 * 6. get tags of a video (user_video_tag table)
 * 7. Get videos with similar title (video table and user_video table)
 */