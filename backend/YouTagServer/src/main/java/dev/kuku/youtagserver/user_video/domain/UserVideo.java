package dev.kuku.youtagserver.user_video.domain;

import dev.kuku.youtagserver.shared.constants.DbConst;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Getter
@Table(name = DbConst.UserVideo.TABLE_NAME)
public class UserVideo {
    @Id
    String id;
    @Column(name = DbConst.UserVideo.USER_ID)
    String userId;
    @Column(name = DbConst.UserVideo.VIDEO_ID)
    String videoId;
}
