package dev.kuku.youtagserver.user_tag.api.services;

import dev.kuku.youtagserver.shared.api.services.Service;
import dev.kuku.youtagserver.user_tag.api.dtos.UserTagDTO;
import dev.kuku.youtagserver.user_tag.domain.UserTag;

import java.util.List;

public interface UserTagService extends Service<UserTag, UserTagDTO> {
    /**
     * Add tags to user.
     *
     * @param userId user to add the tags to
     * @param tags   tags to add
     */
    void addTagsToUser(String userId, List<String> tags);

    /**
     * Get specified tags of a user. If user-tag doesn't exist. It will not be returned
     *
     * @param userId user Id
     * @param tags   tags to check
     * @param skip   how many to skip
     * @param limit  how many to limit
     * @return list of tags of the user that exists
     */
    List<String> getSpecificTagsOfUser(String userId, List<String> tags);

    /**
     * Get all tags of user with pagination.
     *
     * @param userId userId
     * @param skip   how many to skip
     * @param limit  how many to limit to
     * @return list of tags of user
     */
    List<String> getAllTagsOfUser(String userId, int skip, int limit);

    /**
     * Remove all tags of user
     * @param userId userId
     */
    void deleteAllTagsOfUser(String userId);
}
