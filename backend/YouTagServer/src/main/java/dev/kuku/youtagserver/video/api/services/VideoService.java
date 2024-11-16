package dev.kuku.youtagserver.video.api.services;


import dev.kuku.youtagserver.shared.application.Service;
import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.exceptions.VideoAlreadyExists;
import dev.kuku.youtagserver.video.api.exceptions.VideoDTOHasNullValues;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.domain.Video;

import java.util.List;

public interface VideoService extends Service<Video, VideoDTO> {
    /**
     * Get video by id
     */
    VideoDTO getVideoInfo(String id) throws VideoNotFound;

    void addVideo(VideoDTO video) throws VideoAlreadyExists;

    /**
     * Update info of existing video
     *
     * @throws VideoNotFound if video doesn't exist in repo
     */
    void updateVideo(VideoDTO video) throws VideoNotFound;

    /**
     * Delete a video from repo
     *
     * @param id id of the video to delete
     * @throws VideoNotFound if video doesn't exist in repo
     */
    void deleteVideo(String id) throws VideoNotFound;

}
