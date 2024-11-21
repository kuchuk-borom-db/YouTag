package dev.kuku.youtagserver.user_tag.infrastructure;

import dev.kuku.youtagserver.user_tag.domain.UserTag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

public interface TagRepo extends CrudRepository<UserTag, String> {

    List<UserTag> findAllByUserIdAndTagIn(String userId, Collection<String> tags);

    List<UserTag> findAllByUserId(String userId, Pageable of);
}
