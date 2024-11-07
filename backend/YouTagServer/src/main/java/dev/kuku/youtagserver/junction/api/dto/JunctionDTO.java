package dev.kuku.youtagserver.junction.api.dto;

import dev.kuku.youtagserver.junction.api.exceptions.JunctionDTOHasNullValues;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JunctionDTO {
    final String userId;
    final String videoId;
    final String tag;

    public JunctionDTO(String userId, String videoId, String tag) throws JunctionDTOHasNullValues {
        this.userId = userId;
        this.videoId = videoId;
        this.tag = tag;

        if (userId == null || userId.isBlank() ||
                videoId == null || videoId.isBlank() ||
                tag == null || tag.isBlank()) {
            throw new JunctionDTOHasNullValues(this);
        }
    }
}
