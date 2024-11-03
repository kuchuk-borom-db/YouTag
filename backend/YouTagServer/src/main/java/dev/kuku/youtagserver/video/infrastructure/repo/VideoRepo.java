package dev.kuku.youtagserver.video.infrastructure.repo;

import dev.kuku.youtagserver.video.domain.entity.Video;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepo extends CrudRepository<Video, String> {
}
