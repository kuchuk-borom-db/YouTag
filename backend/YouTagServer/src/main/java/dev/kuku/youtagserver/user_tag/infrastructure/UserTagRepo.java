package dev.kuku.youtagserver.user_tag.infrastructure;

import dev.kuku.youtagserver.user_tag.domain.UserTag;
import dev.kuku.youtagserver.user_tag.domain.UserTagId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;

public interface UserTagRepo extends CrudRepository<UserTag, UserTagId> {

    List<UserTag> findAllByUserId(String userId, PageRequest of);

    void deleteAllByUserId(String userId);

    void deleteAllByTagIn(Set<String> tags);

    long countAllByUserId(String userId);

    List<UserTag> findAllByUserIdAndTagContaining(String userId, String tag, Pageable pageRequest);
}
