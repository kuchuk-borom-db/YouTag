package dev.kuku.youtagserver.video.api.dto;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;


@Getter
@ToString
public class VideoDTO {
    String id;
    String title;
    String description;
    String thumbnail;
    LocalDateTime updated;

    public VideoDTO(String id, String title, String description, String thumbnail) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.thumbnail = thumbnail;
    }
}
