package dev.kuku.youtagserver.junction.api.services;

import dev.kuku.youtagserver.junction.api.dtos.TagDTO;
import dev.kuku.youtagserver.junction.domain.Tag;
import dev.kuku.youtagserver.shared.application.Service;

import java.util.List;

public interface TagService extends Service<Tag, TagDTO> {

    void addTagsToVideo(String userId, String videoId, List<String> tags);

    List<TagDTO> getTagsOfVideo(String userId, String videoId);
}
