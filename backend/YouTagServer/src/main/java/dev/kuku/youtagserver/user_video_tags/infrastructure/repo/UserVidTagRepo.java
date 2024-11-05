package dev.kuku.youtagserver.user_video_tags.infrastructure.repo;

import dev.kuku.youtagserver.user_video_tags.domain.entity.UserVidTag;
import dev.kuku.youtagserver.user_video_tags.domain.entity.UserVidTagId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserVidTagRepo extends CrudRepository<UserVidTag, UserVidTagId> {

    /**
     * Finds a UserVidTag by the user email and video id.
     *
     * @param userEmail the user email
     * @param videoId   the video id
     * @return the UserVidTag, or null if not found
     */
    UserVidTag findUserVidTagByUserEmailAndVideoId(String userEmail, String videoId);

    /**
     * Finds all UserVidTags for the given user email.
     *
     * @param userEmail the user email
     * @return a list of UserVidTags for the user
     */
    List<UserVidTag> findAllByUserEmail(String userEmail);

    /**
     * Finds all UserVidTags for the given video id.
     *
     * @param videoId the video id
     * @return a list of UserVidTags for the video
     */
    List<UserVidTag> findAllByVideoId(String videoId);

    /**
     * Finds all UserVidTags for the given tag.
     *
     * @param tag the tag
     * @return a list of UserVidTags for the tag
     */
    List<UserVidTag> findAllByTagsContaining(String[] tag);

    /**
     * Finds all UserVidTags for the given user email and tag.
     *
     * @param userEmail the user email
     * @param tag       the tag
     * @return a list of UserVidTags for the user and tag
     */
    List<UserVidTag> findAllByUserEmailAndTagsContaining(String userEmail, String[] tag);

}