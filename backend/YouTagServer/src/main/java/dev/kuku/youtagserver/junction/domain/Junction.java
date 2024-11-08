package dev.kuku.youtagserver.junction.domain;


import dev.kuku.youtagserver.shared.constants.DbConst;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = DbConst.Junction.TABLE_NAME, indexes = @Index(name = "idx_junction_user_video_tag", columnList = "user_id, video_id, tag"))
@IdClass(JunctionId.class)
@ToString
public class Junction {
    @Id
    @Column(name = DbConst.Junction.USER_ID)
    String userId;
    @Id
    @Column(name = DbConst.Junction.VIDEO_ID)
    String videoId;
    @Column(name = DbConst.Junction.TAG, updatable = false)
    String tag;
}
