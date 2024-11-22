package dev.kuku.youtagserver.user_video.infrastructure;

import dev.kuku.youtagserver.user_video.domain.UserVideo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserVideoRepo extends CrudRepository<UserVideo, String> {

    List<UserVideo> getAllByUserId(String userId, Pageable pageable);

    /**
     * Get all entries with matching userId and VideoID
     *
     * @param userId   userId
     * @param videoIds videoIds to look for
     */
    List<UserVideo> findAllByUserIdAndVideoIdIn(String userId, List<String> videoIds);

    UserVideo findByUserIdAndVideoId(String userId, String videoId);

    void deleteAllByUserIdAndVideoIdIn(String userId, List<String> videoIds);

}
