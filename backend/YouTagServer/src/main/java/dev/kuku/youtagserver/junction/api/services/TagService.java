package dev.kuku.youtagserver.junction.api.services;

import dev.kuku.youtagserver.junction.api.dtos.TagDTO;
import dev.kuku.youtagserver.junction.domain.Tag;
import dev.kuku.youtagserver.shared.application.Service;

import java.util.List;

public interface TagService extends Service<Tag, TagDTO> {

    void addTagsToVideo(String userId, String videoId, List<String> tags);

    List<TagDTO> getAllTagsOfUser(String userId, int skip, int limit);

    List<TagDTO> getTagsOfVideo(String userId, String videoId);

    List<TagDTO> getVideosWithTag(String userId, List<String> tags, int skip, int limit);

    void deleteTagsFromAllVideos(String userId, List<String> tags);

    void deleteTagsFromVideos(String userId, List<String> tags, List<String> videoIds);

}
