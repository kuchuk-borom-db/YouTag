package dev.kuku.youtagserver.user_video_tags.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserVidTagId implements java.io.Serializable {  // Made class public
    private String userEmail;
    private String videoId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserVidTagId that = (UserVidTagId) o;
        return java.util.Objects.equals(userEmail, that.userEmail) &&
                java.util.Objects.equals(videoId, that.videoId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(userEmail, videoId);
    }
}
