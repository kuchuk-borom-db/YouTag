package dev.kuku.youtagserver.video.api.services;


import dev.kuku.youtagserver.shared.application.Service;
import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.exceptions.InvalidVideoId;
import dev.kuku.youtagserver.video.api.exceptions.VideoAlreadyExists;
import dev.kuku.youtagserver.video.api.exceptions.VideoDTOHasNullValues;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.domain.Video;

public interface VideoService extends Service<Video, VideoDTO> {
    /**
     * Get video by id
     */
    VideoDTO getVideo(String id) throws VideoNotFound, VideoDTOHasNullValues;

    /**
     * Add a new video with empty title, description, thumbnail and current time as updated time
     *
     * @throws VideoAlreadyExists if video already exists in repo
     * @throws InvalidVideoId     if videoId is invalid
     */
    void addVideo(String id) throws VideoAlreadyExists, InvalidVideoId, VideoDTOHasNullValues;

    /**
     * Update info of existing video
     *
     * @throws VideoNotFound if video doesn't exist in repo
     */
    void updateVideo(VideoDTO video) throws VideoNotFound, VideoDTOHasNullValues;

    /**
     * Delete a video from repo
     *
     * @param id id of the video to delete
     * @throws VideoNotFound if video doesn't exist in repo
     */
    void deleteVideo(String id) throws VideoNotFound, VideoDTOHasNullValues;

    //TODO plural operations such as addVideos, getVideos(), updateVideos(), deleteVideos(). Skipping because there is no use for it yet.

}
