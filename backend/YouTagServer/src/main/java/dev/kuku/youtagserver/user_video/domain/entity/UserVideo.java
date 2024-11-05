package dev.kuku.youtagserver.user_video.domain.entity;

import dev.kuku.youtagserver.shared.constants.DbConst;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = DbConst.UserVideo.TABLE_NAME)
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@IdClass(UserVideoId.class)
public class UserVideo {
    @Id
    @Column(name = DbConst.UserVideo.USER_ID, length = 250)
    String userId;
    @Id
    @Column(name = DbConst.UserVideo.VIDEO_ID, length = 50)
    String videoId;
    @Column(name = DbConst.CommonColumn.CREATED)
    LocalDateTime created;
}
