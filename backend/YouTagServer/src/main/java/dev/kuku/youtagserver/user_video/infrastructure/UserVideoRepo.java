package dev.kuku.youtagserver.user_video.infrastructure;

import dev.kuku.youtagserver.user_video.domain.entity.UserVideo;
import dev.kuku.youtagserver.user_video.domain.entity.UserVideoId;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserVideoRepo extends CrudRepository<UserVideo, UserVideoId> {
    UserVideo findByUserIdAndVideoId(String userId, String videoId);
    List<UserVideo> findByUserId(String userId);
}
