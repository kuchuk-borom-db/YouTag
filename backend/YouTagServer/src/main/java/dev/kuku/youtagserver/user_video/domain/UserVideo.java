package dev.kuku.youtagserver.user_video.domain;

import dev.kuku.youtagserver.shared.api.constants.DbConst;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Entity
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Table(name = DbConst.UserVideo.TABLE_NAME)
@IdClass(UserVideoId.class)
@Getter
@ToString
public class UserVideo {
    @Id
    @Column(name = DbConst.CommonColumn.USER_ID)
    String userId;
    @Id
    @Column(name = DbConst.CommonColumn.VIDEO_ID)
    String videoId;
}
