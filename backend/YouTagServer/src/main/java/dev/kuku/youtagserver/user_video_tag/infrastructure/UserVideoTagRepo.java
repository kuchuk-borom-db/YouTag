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

    @Query(value = """
    SELECT videoId
    FROM UserVideoTag
    WHERE userId = :userId AND tag IN :tags
    GROUP BY videoId
    HAVING COUNT(DISTINCT tag) = :tagsCount
""")
    List<String> findVideoIdsWithAllTags(@Param("userId") String userId,
                                         @Param("tags") List<String> tags,
                                         @Param("tagsCount") long tagsCount,
                                         Pageable pageable);


    @Query("""
SELECT COUNT(DISTINCT v.videoId) 
FROM UserVideoTag v
WHERE v.userId = :userId AND v.videoId IN (
    SELECT vInner.videoId 
    FROM UserVideoTag vInner
    WHERE vInner.userId = :userId AND vInner.tag IN :tags
    GROUP BY vInner.videoId
    HAVING COUNT(DISTINCT vInner.tag) = :tagCount
)
""")
    long countDistinctVideosByUserIdAndTags(
            @Param("userId") String userId,
            @Param("tags") List<String> tags,
            @Param("tagCount") long tagCount
    );

}
