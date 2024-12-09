package dev.kuku.youtagserver.user_video_tag.infrastructure;

import dev.kuku.youtagserver.user_video_tag.domain.UserVideoTag;
import dev.kuku.youtagserver.user_video_tag.domain.UserVideoTagId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface UserVideoTagRepo extends CrudRepository<UserVideoTag, UserVideoTagId> {
    List<UserVideoTag> findAllByUserIdAndVideoId(String userId, String videoId);

    void deleteAllByUserIdAndVideoIdInAndTagIn(String userId, Collection<String> videoIds, Collection<String> tags);

    List<UserVideoTag> deleteAllByUserIdAndVideoIdIn(String userId, Collection<String> videoIds);

    void deleteAllByUserId(String userId);

    List<UserVideoTag> findAllByUserIdAndTagIn(String userId, List<String> tags, Pageable of);

    List<UserVideoTag> findAllByUserIdAndVideoIdIn(String userId, Collection<String> videoIds, Pageable of);

    List<UserVideoTag> deleteAllByVideoIdIn(List<String> videoIds);

    List<UserVideoTag> findAllByTagIn(List<String> tags);


    List<UserVideoTag> findAllByUserIdAndTagIn(String userId, List<String> tags);

    @Query(value = "SELECT COUNT(DISTINCT video_id) FROM user_video_tag WHERE user_id = :userId AND video_id IN (" +
            "SELECT video_id FROM user_video_tag " +
            "WHERE user_id = :userId AND tag IN :tags " +
            "GROUP BY video_id " +
            "HAVING COUNT(DISTINCT tag) = :tagCount)", nativeQuery = true)
    long countDistinctVideosByUserIdAndTags(
            @Param("userId") String userId,
            @Param("tags") List<String> tags,
            @Param("tagCount") long tagCount
    );
}
