package dev.kuku.youtagserver.user_video_tag.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserVideoTagId {
    String userId;
    String videoId;
    String tag;
}
