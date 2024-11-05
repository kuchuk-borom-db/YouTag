package dev.kuku.youtagserver.user_video_tag.domain.entity;

import dev.kuku.youtagserver.shared.constants.DbConst;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Service;

@Entity
@Table(name = DbConst.UserVideoTag.TABLE_NAME)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Service
@ToString
@IdClass(UserVideoTagId.class)
public class UserVideoTag {
    @Id
    @Column(name = DbConst.UserVideoTag.USER_ID)
    String userId;
    @Id
    @Column(name = DbConst.UserVideoTag.VIDEO_ID)
    String videoId;
    @Id
    @Column(name = DbConst.UserVideoTag.TAG)
    String tag;
}
