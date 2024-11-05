package dev.kuku.youtagserver.video.api.services;


import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;

public interface VideoService {
    void updateVideoInfo(VideoDTO videoDTO) throws VideoNotFound;
}
