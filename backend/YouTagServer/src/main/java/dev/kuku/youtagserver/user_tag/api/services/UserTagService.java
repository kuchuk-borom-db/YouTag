package dev.kuku.youtagserver.user_tag.api.services;

import dev.kuku.youtagserver.shared.application.Service;
import dev.kuku.youtagserver.user_tag.api.dtos.UserTagDTO;
import dev.kuku.youtagserver.user_tag.domain.UserTag;

import java.util.List;

public interface UserTagService extends Service<UserTag, UserTagDTO> {
    /**
     * Add tags to user. If tag already exists it will be ignored.
     *
     * @param userId userId
     * @param tags   tags to add to user
     * @Return Added user Tag
     */
    void addTagsToUser(String userId, List<String> tags);

    /**
     * Get all the tags of user
     *
     * @param userId userId
     * @param skip   how many to skip
     * @param limit  how many to limit to
     * @return tags of user
     */
    List<UserTagDTO> getAllTagsOfUser(String userId, int skip, int limit);

    List<UserTagDTO> getTagsOfUser(String currentUserId, List<String> tags);
}
