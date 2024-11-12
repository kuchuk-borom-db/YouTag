package dev.kuku.youtagserver.video.infrastructure;

import dev.kuku.youtagserver.video.domain.Video;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface VideoRepo extends CrudRepository<Video, String> {
    List<Video> findAllByIdIn(Collection<String> id);
}
