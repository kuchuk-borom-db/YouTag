package dev.kuku.youtagserver.user_video.infrastructure;

import dev.kuku.youtagserver.user_video.domain.UserVideo;
import dev.kuku.youtagserver.user_video.domain.UserVideoId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserVideoRepo extends CrudRepository<UserVideo, UserVideoId> {
    List<UserVideo> findAllByUserId(String userId, PageRequest of);

    List<UserVideo> findAllByUserIdAndVideoIdIn(String userId, List<String> videoIds);

    void deleteAllByUserIdAndVideoIdIn(String userId, List<String> videoIds);
}
