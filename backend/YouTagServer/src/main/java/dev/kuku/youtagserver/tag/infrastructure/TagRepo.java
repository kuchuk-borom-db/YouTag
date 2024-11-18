package dev.kuku.youtagserver.tag.infrastructure;

import dev.kuku.youtagserver.tag.domain.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TagRepo extends CrudRepository<Tag, String> {
    /*
    It is possible to have return type of delete as List to get a list of deleted items back.
     */
    void deleteAllByUserId(String userId);

    void deleteAllByUserIdAndTagIn(String userId, List<String> tags);

    void deleteAllByUserIdAndVideoIdInAndTagIn(String userId, List<String> videoIds, List<String> tags);

    void deleteAllByUserIdAndVideoIdIn(String userId, List<String> videoId);

    void deleteAllByVideoId(String videoId);

    List<Tag> findAllByUserId(String userId, Pageable pageRequest, Sort sort);

    List<Tag> findAllByUserIdAndTagIn(String userId, List<String> tags, Pageable of);

    List<Tag> findAllByUserIdAndVideoId(String userId, String videoId);

    List<Tag> findAllByVideoId(String videoId);

    List<Tag> findAllByUserIdAndTagContaining(String userId, String tag, Pageable page, Sort sort);
}
