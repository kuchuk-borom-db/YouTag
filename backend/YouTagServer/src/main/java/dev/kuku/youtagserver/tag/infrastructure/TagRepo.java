package dev.kuku.youtagserver.tag.infrastructure;

import dev.kuku.youtagserver.tag.domain.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TagRepo extends CrudRepository<Tag, String> {
    List<Tag> deleteAllByUserId(String userId);

    List<Tag> deleteAllByUserIdAndTagIn(String userId, List<String> tags);

    List<Tag> deleteAllByUserIdAndVideoIdInAndTagIn(String userId, List<String> videoIds, List<String> tags);

    List<Tag> deleteAllByUserIdAndVideoIdIn(String userId, List<String> videoId);

    List<Tag> deleteAllByVideoIdIn(List<String> ids);


    List<Tag> findAllByUserId(String userId, Pageable pageRequest);

    List<Tag> findAllByUserIdAndTagIn(String userId, List<String> tags, Pageable of);

    List<Tag> findAllByUserIdAndVideoIdIn(String userId, List<String> videos, Pageable of);

    long countByUserId(String userId);

    List<Tag> findAllByUserIdAndVideoId(String userId, String videoId);
}
