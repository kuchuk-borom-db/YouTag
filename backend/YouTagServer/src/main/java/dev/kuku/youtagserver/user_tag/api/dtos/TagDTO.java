package dev.kuku.youtagserver.user_tag.api.dtos;

import dev.kuku.youtagserver.user_tag.api.exceptions.TagDTOHasNullValues;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TagDTO {
    final String userId;
    final String videoId;
    final String tag;

    public TagDTO(String userId, String videoId, String tag) throws TagDTOHasNullValues {
        this.userId = userId;
        this.videoId = videoId;
        this.tag = tag;

        if (userId == null || userId.isBlank() ||
                videoId == null || videoId.isBlank() ||
                tag == null || tag.isBlank()) {
            throw new TagDTOHasNullValues(this);
        }
    }
}
