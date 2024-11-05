package dev.kuku.youtagserver.user_video_tags.infrastructure.repo;

import dev.kuku.youtagserver.user_video_tags.domain.entity.UserVidTag;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserVidTagRepo extends CrudRepository<UserVidTag, String> {
    //Name needs to match Entity's naming
    UserVidTag findUserVidTagByUserEmailAndVideoId(String user_email, String video_id);
}
