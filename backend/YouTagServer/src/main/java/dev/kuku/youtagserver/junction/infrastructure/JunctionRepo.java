package dev.kuku.youtagserver.junction.infrastructure;

import dev.kuku.youtagserver.junction.domain.Junction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface JunctionRepo extends CrudRepository<Junction, String> {
    List<Junction> deleteAllByUserId(String userId);

    List<Junction> deleteAllByUserIdAndTagIn(String userId, List<String> tags);

    List<Junction> deleteAllByUserIdAndVideoIdInAndTagIn(String userId, List<String> videoIds, List<String> tags);

    List<Junction> deleteAllByUserIdAndVideoIdIn(String userId, List<String> videoId);

    List<Junction> deleteAllByVideoIdIn(List<String> ids);


    List<Junction> findAllByUserId(String userId, Pageable pageRequest);

    List<Junction> findAllByUserIdAndTagIn(String userId, List<String> tags, Pageable of);

    List<Junction> findAllByUserIdAndVideoIdIn(String userId, List<String> videos, Pageable of);

    long countByUserId(String userId);
}
