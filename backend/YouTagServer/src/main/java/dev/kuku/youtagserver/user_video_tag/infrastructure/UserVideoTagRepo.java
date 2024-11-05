package dev.kuku.youtagserver.user_video_tag.infrastructure;

import dev.kuku.youtagserver.user_video_tag.domain.entity.UserVideoTag;
import dev.kuku.youtagserver.user_video_tag.domain.entity.UserVideoTagId;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserVideoTagRepo extends CrudRepository<UserVideoTag, UserVideoTagId> {
    /**
     * Find videos that belong to userId and has tag
     */
    List<UserVideoTag> findAllByUserIdAndTag(String userId, String tag);

    UserVideoTag findByUserIdAndTagAndVideoId(String userId, String tag, String videoId);
}
