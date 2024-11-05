package dev.kuku.youtagserver.webscraper.api.services;

import dev.kuku.youtagserver.webscraper.api.dto.YoutubeVideoInfoDto;

public interface YoutubeScrapperService {
    YoutubeVideoInfoDto getYoutubeVideoInfo(String videoId);
    boolean validateVideo(String id);
}
