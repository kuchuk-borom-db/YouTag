package dev.kuku.youtagserver.user_video_tag.infrastructure;

import dev.kuku.youtagserver.user_video_tag.domain.UserVideoTag;
import dev.kuku.youtagserver.user_video_tag.domain.UserVideoTagId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

public interface UserVideoTagRepo extends CrudRepository<UserVideoTag, UserVideoTagId> {
    List<UserVideoTag> findAllByUserIdAndVideoId(String userId, String videoId);

    void deleteAllByUserIdAndVideoIdInAndTagIn(String userId, Collection<String> videoIds, Collection<String> tags);

    void deleteAllByUserIdAndTagIn(String userId, Collection<String> tags);

    void deleteAllByUserIdAndVideoIdIn(String userId, Collection<String> videoIds);

    List<UserVideoTag> findAllByUserIdAndTagIn(String userId, List<String> tags, Pageable of);

    List<UserVideoTag> findAllByUserIdAndVideoIdIn(String userId, Collection<String> videoIds,Pageable of);
}
