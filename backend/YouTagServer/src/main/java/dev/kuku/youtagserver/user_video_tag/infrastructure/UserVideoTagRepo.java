package dev.kuku.youtagserver.user_video_tag.infrastructure;

import dev.kuku.youtagserver.user_video_tag.domain.UserVideoTag;
import dev.kuku.youtagserver.user_video_tag.domain.UserVideoTagId;
import org.springframework.data.repository.CrudRepository;

public interface UserVideoTagRepo extends CrudRepository<UserVideoTag, UserVideoTagId> {
}
