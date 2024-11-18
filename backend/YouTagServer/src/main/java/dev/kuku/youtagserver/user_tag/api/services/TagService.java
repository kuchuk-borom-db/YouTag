package dev.kuku.youtagserver.user_tag.api.services;

import dev.kuku.youtagserver.shared.application.Service;
import dev.kuku.youtagserver.user_tag.api.dtos.TagDTO;
import dev.kuku.youtagserver.user_tag.domain.Tag;

import java.util.List;

public interface TagService extends Service<Tag, TagDTO> {

    void addTagsToVideo(String userId, String videoId, List<String> tags);

    List<TagDTO> getAllTagsOfUser(String userId, int skip, int limit);

    List<TagDTO> getAllTagsOfUserContaining(String userId, String containing, int skip, int limit);

    List<TagDTO> getTagsOfVideo(String userId, String videoId);

    List<TagDTO> getVideosWithTag(String userId, List<String> tags, int skip, int limit);

    void deleteAllTagsOfUser(String userId);

    void DeleteTagsFromAllVideosOfUser(String userId, List<String> tags);

    void deleteTagsFromVideosOfUser(String userId, List<String> tags, List<String> videoIds);

    void deleteAllTagsOfVideoOfUser(String userId, String videoId);

    void deleteAllTagsOfAllUsersOfVideo(String videoId);
}
