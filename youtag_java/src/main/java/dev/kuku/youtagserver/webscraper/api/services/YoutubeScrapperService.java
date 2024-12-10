package dev.kuku.youtagserver.webscraper.api.services;

import dev.kuku.youtagserver.webscraper.api.dto.YoutubeVideoInfoDto;
import dev.kuku.youtagserver.webscraper.api.exceptions.InvalidVideoId;

public interface YoutubeScrapperService {
    YoutubeVideoInfoDto getYoutubeVideoInfo(String videoId) throws InvalidVideoId;
}
