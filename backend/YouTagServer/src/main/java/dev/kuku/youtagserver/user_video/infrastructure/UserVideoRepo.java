package dev.kuku.youtagserver.user_video.infrastructure;

import dev.kuku.youtagserver.user_video.domain.UserVideo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserVideoRepo extends CrudRepository<UserVideo, String> {

    List<UserVideo> getAllByUserId(String userId, Pageable pageable);

    UserVideo getByUserIdAndVideoId(String userId, String videoId);

    void deleteByUserIdAndVideoId(String userId, String videoId);

    void deleteByUserId(String userId);

    void deleteAllByVideoId(String videoId);

}
