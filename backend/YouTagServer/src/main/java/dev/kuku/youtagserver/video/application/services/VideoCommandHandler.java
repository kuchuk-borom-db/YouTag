package dev.kuku.youtagserver.video.application.services;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.services.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoCommandHandler {
    private final VideoService videoService;

    public VideoDTO getVideoInfo(String videoId) throws ResponseException {
        return videoService.getVideo(videoId);
    }
}
