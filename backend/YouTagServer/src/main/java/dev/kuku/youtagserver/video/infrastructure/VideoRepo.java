package dev.kuku.youtagserver.video.infrastructure;

import dev.kuku.youtagserver.video.domain.Video;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepo extends CrudRepository<Video, String> {
}
