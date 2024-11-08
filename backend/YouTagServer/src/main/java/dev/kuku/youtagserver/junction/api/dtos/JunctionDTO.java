package dev.kuku.youtagserver.junction.api.dtos;

import dev.kuku.youtagserver.junction.api.exceptions.JunctionDTOHasNullValues;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
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
