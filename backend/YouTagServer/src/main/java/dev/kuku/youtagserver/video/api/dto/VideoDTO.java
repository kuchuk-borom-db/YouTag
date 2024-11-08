package dev.kuku.youtagserver.video.api.dto;

import dev.kuku.youtagserver.video.api.exceptions.VideoDTOHasNullValues;
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

    public VideoDTO(String id, String title, String description, String thumbnail) throws VideoDTOHasNullValues {
        this.id = id;
        this.title = title;
        this.description = description;
        this.thumbnail = thumbnail;

        if (id == null || id.isEmpty() || id.isBlank()) {
            throw new VideoDTOHasNullValues(this);
        }
        if (title == null || title.isEmpty() || title.isBlank()) {
            throw new VideoDTOHasNullValues(this);
        }
        if (description == null || description.isEmpty() || description.isBlank()) {
            throw new VideoDTOHasNullValues(this);
        }
        if (thumbnail == null || thumbnail.isEmpty() || thumbnail.isBlank()) {
            throw new VideoDTOHasNullValues(this);
        }
    }
}
