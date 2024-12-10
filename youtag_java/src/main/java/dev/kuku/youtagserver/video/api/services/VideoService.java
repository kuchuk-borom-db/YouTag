package dev.kuku.youtagserver.video.api.services;


import dev.kuku.youtagserver.shared.api.services.Service;
import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.exceptions.VideoAlreadyExists;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.domain.Video;

import java.util.List;
import java.util.Set;

public interface VideoService extends Service<Video, VideoDTO> {
    /**
     * Get video by id
     */
    VideoDTO getVideoInfo(String id) throws VideoNotFound;

    /**
     * Get video by ids
     */
    List<VideoDTO> getVideoInfos(List<String> ids);

    void addVideo(VideoDTO video) throws VideoAlreadyExists;

    /**
     * Add videos by Ids
     */
    void addVideos(List<String> ids);

    /**
     * Update info of existing video
     *
     * @throws VideoNotFound if video doesn't exist in repo
     */
    void updateVideo(VideoDTO video) throws VideoNotFound;

    /**
     * Update videos
     * @param videos videos to update
     */
    void updateVideos(List<VideoDTO> videos);

    /**
     * Delete specified video ids
     *
     * @param videoIds videos to delete
     */
    void deleteSpecifiedVideos(Set<String> videoIds);

}
