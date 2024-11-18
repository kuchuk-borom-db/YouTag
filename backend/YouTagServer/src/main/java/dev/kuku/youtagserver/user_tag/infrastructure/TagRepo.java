package dev.kuku.youtagserver.user_tag.infrastructure;

import dev.kuku.youtagserver.user_tag.domain.Tag;
import dev.kuku.youtagserver.user_video.api.UserVideoDTO;
import dev.kuku.youtagserver.user_video.api.exceptions.UserVideoAlreadyLinked;
import dev.kuku.youtagserver.user_video.api.exceptions.UserVideoNotFound;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TagRepo extends CrudRepository<Tag, String> {
    /*
    It is possible to have return type of delete as List to get a list of deleted items back.
     */
    void deleteAllByUserId(String userId);

    void deleteAllByUserIdAndTagIn(String userId, List<String> tags);

    void deleteAllByUserIdAndVideoIdInAndTagIn(String userId, List<String> videoIds, List<String> tags);

    void deleteAllByUserIdAndVideoIdIn(String userId, List<String> videoId);

    void deleteAllByVideoId(String videoId);

    List<Tag> findAllByUserId(String userId, Pageable pageRequest);

    List<Tag> findAllByUserIdAndTagIn(String userId, List<String> tags, Pageable of);

    List<Tag> findAllByUserIdAndVideoId(String userId, String videoId);

    List<Tag> findAllByVideoId(String videoId);

    List<Tag> findAllByUserIdAndTagContaining(String userId, String tag, Pageable page);



    void connectTagToUser(String userId, String tagId) throws UserVideoAlreadyLinked;

    void disconnectTagFromUser(String userId, String tagId) throws UserVideoNotFound;

    void removeAllTagsOfUser(String userId);

    void removeTagsFromAllUsers(String tagId);

    List<UserVideoDTO> getTagsOfUser(String userId, int skip, int limit);

    boolean isTagConnectedToUser(String userId, String tagId);
}
