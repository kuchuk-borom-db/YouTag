package dev.kuku.youtagserver.user_video_tags.api.services;

import dev.kuku.youtagserver.user_video_tags.api.dto.UserVidTagDto;
import dev.kuku.youtagserver.user_video_tags.api.exceptions.UserAndVideoAlreadyLinked;
import dev.kuku.youtagserver.user_video_tags.api.exceptions.UserAndVideoLinkNotFound;

import java.util.List;

public interface UserVidTagService {

    /**
     * Links a user and a video, creating a new UserVidTag record.
     *
     * @param userEmail the user email
     * @param videoId   the video id
     * @throws UserAndVideoAlreadyLinked if the user and video are already linked
     */
    void linkUserAndVideo(String userEmail, String videoId) throws UserAndVideoAlreadyLinked;

    /**
     * Adds tags to an existing UserVidTag record.
     *
     * @param userEmail the user email
     * @param videoId   the video id
     * @param tags      the tags to add
     * @throws UserAndVideoLinkNotFound if the user and video are not linked
     */
    void addTags(String userEmail, String videoId, String[] tags) throws UserAndVideoLinkNotFound;

    /**
     * Removes tags from an existing UserVidTag record.
     *
     * @param userEmail the user email
     * @param videoId   the video id
     * @param tags      the tags to remove
     * @throws UserAndVideoLinkNotFound if the user and video are not linked
     */
    void removeTags(String userEmail, String videoId, String[] tags) throws UserAndVideoLinkNotFound;

    /**
     * Retrieves a UserVidTag record by the user email and video id.
     *
     * @param userEmail the user email
     * @param videoId   the video id
     * @return the UserVidTagDto
     * @throws UserAndVideoLinkNotFound if the user and video are not linked
     */
    UserVidTagDto getUserAndVideoTag(String userEmail, String videoId) throws UserAndVideoLinkNotFound;

    /**
     * Retrieves all UserVidTag records for the given user email.
     *
     * @param userEmail the user email
     * @return a list of UserVidTagDto
     */
    List<UserVidTagDto> getUserAndVideoTagsByUser(String userEmail);

    /**
     * Retrieves all UserVidTag records for the given video id.
     *
     * @param videoId the video id
     * @return a list of UserVidTagDto
     */
    List<UserVidTagDto> getUserAndVideoTagsByVideo(String videoId);

    /**
     * Retrieves all UserVidTag records that contain the given tag.
     *
     * @param tag the tag
     * @return a list of UserVidTagDto
     */
    List<UserVidTagDto> getUserAndVideoTagsByTag(String tag);

    /**
     * Retrieves all UserVidTag records for the given user email and tag.
     *
     * @param userEmail the user email
     * @param tag       the tag
     * @return a list of UserVidTagDto
     */
    List<UserVidTagDto> getUserAndVideoTagsByUserAndTag(String userEmail, String tag);
}