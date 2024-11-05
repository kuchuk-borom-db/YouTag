package dev.kuku.youtagserver.video.api.services;


import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.exceptions.InvalidVideoIDException;
import dev.kuku.youtagserver.video.api.exceptions.VideoAlreadyExists;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;

public interface VideoService {
    void updateVideoInfo(VideoDTO videoDTO) throws VideoNotFound;

    VideoDTO getVideo(String id) throws VideoNotFound;

    VideoDTO addVideo(String id) throws VideoAlreadyExists, InvalidVideoIDException;
}
