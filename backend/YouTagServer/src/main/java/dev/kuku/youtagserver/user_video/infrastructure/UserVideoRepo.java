package dev.kuku.youtagserver.user_video.infrastructure;

import dev.kuku.youtagserver.user_video.domain.entity.UserVideo;
import dev.kuku.youtagserver.user_video.domain.entity.UserVideoId;
import org.springframework.data.repository.CrudRepository;

public interface UserVideoRepo extends CrudRepository<UserVideo, UserVideoId> {
    UserVideo findUserVideoByUserIdAndVideoId(String userId, String videoId);
}
