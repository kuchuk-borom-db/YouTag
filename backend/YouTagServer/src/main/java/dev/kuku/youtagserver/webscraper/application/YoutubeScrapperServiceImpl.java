package dev.kuku.youtagserver.webscraper.application;

import dev.kuku.youtagserver.webscraper.api.dto.YoutubeVideoInfoDto;
import dev.kuku.youtagserver.webscraper.api.exceptions.InvalidVideoId;
import dev.kuku.youtagserver.webscraper.api.services.YoutubeScrapperService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class YoutubeScrapperServiceImpl implements YoutubeScrapperService {

    @Override
    public YoutubeVideoInfoDto getYoutubeVideoInfo(String videoId) throws InvalidVideoId {
        String url = generateUrl(videoId);
        try {
            Document page = Jsoup.connect(url).get();

            // Extract the title and remove the "- YouTube" suffix if present
            String title = page.title();
            if (title.endsWith(" - YouTube")) {
                title = title.substring(0, title.length() - 10);
            }

            // Extract the description
            String description = page.select("meta[name=description]").attr("content");

            // Extract the thumbnail URL
            String thumbnailUrl = page.select("meta[property=og:image]").attr("content");

            return new YoutubeVideoInfoDto(videoId, title, description, thumbnailUrl);

        } catch (IOException e) {
            log.error("Failed to load video {} with error {}", videoId, e.getMessage());
        }
        throw new InvalidVideoId(videoId);
    }

    private String generateUrl(String videoId) {
        return "https://www.youtube.com/watch?v=" + videoId;
    }
}
