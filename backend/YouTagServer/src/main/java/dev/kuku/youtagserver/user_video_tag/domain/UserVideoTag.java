package dev.kuku.youtagserver.user_video_tag.domain;


import dev.kuku.youtagserver.shared.constants.DbConst;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Table(name = DbConst.UserVideoTag.TABLE_NAME)
public class UserVideoTag {
    @Id
    @Column(name = DbConst.CommonColumn.ID)
    String id;
    @Column(name = DbConst.CommonColumn.USER_ID)
    String userId;
    @Column(name = DbConst.CommonColumn.VIDEO_ID)
    String videoId;
    @Column(name = DbConst.CommonColumn.TAG_ID)
    String tagId;
}
