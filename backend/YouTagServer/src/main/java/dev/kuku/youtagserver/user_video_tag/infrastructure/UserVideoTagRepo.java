package dev.kuku.youtagserver.user_video_tag.infrastructure;

import dev.kuku.youtagserver.user_video_tag.domain.entity.UserVideoTag;
import dev.kuku.youtagserver.user_video_tag.domain.entity.UserVideoTagId;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserVideoTagRepo extends CrudRepository<UserVideoTag, UserVideoTagId> {

    void deleteAllByUserIdAndVideoId(String userId, String videoId);


    List<UserVideoTag> findAllByUserIdAndVideoId(String userId, String videoId);

    //Allows passing in many tags at once
    List<UserVideoTag> findAllByUserIdAndTagIn(String userId, List<String> tags);

    List<UserVideoTag> findAllByUserId(String userId);

    /*
     * "In" keyword allows you to pass list as get result in one query
     */
    @Modifying
    List<UserVideoTag> deleteAllByUserIdAndTagIn(String userId, List<String> tags);

    @Modifying
    List<UserVideoTag> deleteAllByUserIdAndVideoIdAndTagIn(String userId, String videoId, List<String> tags);
}
