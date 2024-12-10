package dev.kuku.youtagserver.webscraper.application;

import dev.kuku.youtagserver.webscraper.api.exceptions.InvalidVideoId;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
class YoutubeScrapperServiceImplTest {
    final YoutubeScrapperServiceImpl scrapperService = new YoutubeScrapperServiceImpl();

    @Test
    void testYoutubeScrapper() throws InvalidVideoId {
        var info = scrapperService.getYoutubeVideoInfo("-wLYuox7YE8");
        log.info(info.toString());
        Assertions.assertNotNull(info);
        Assertions.assertEquals(info.title(), "[Original] Weeping Angel | Kuku Pie (Kuchuk Borom Debbarma)");
    }

}