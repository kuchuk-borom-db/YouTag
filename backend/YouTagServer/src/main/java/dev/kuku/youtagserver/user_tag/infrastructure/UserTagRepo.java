package dev.kuku.youtagserver.user_tag.infrastructure;

import dev.kuku.youtagserver.user_tag.domain.UserTag;
import dev.kuku.youtagserver.user_tag.domain.UserTagId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserTagRepo extends CrudRepository<UserTag, UserTagId> {
    List<UserTag> findAllByUserIdAndTagIn(String userId, List<String> tags);

    List<UserTag> findAllByUserId(String userId, PageRequest of);

    void deleteAllByUserId(String userId);
}
