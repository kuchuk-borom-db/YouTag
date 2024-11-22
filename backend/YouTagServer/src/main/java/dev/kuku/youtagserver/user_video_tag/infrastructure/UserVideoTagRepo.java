package dev.kuku.youtagserver.user_video_tag.infrastructure;

import dev.kuku.youtagserver.user_video_tag.domain.UserVideoTag;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserVideoTagRepo extends CrudRepository<UserVideoTag, String> {
    List<UserVideoTag> findAllByUserIdAndVideoId(String userId, String videoId);
}
