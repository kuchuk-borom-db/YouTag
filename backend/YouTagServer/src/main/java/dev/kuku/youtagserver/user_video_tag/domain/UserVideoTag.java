package dev.kuku.youtagserver.user_video_tag.domain;

import dev.kuku.youtagserver.shared.api.constants.DbConst;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = DbConst.UserVideoTag.TABLE_NAME)
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@IdClass(UserVideoTagId.class)
public class UserVideoTag {
    @Id
    @Column(name = DbConst.CommonColumn.USER_ID)
    String userId;
    @Id
    @Column(name = DbConst.CommonColumn.VIDEO_ID)
    String videoId;
    @Id
    @Column(name = DbConst.CommonColumn.TAG)
    String tag;
}
