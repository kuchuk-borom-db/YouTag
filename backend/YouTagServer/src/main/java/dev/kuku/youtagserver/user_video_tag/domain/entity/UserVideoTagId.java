package dev.kuku.youtagserver.user_video_tag.domain.entity;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@EqualsAndHashCode
public class UserVideoTagId implements Serializable {
    String userId;
    String videoId;
    String tag;
}
