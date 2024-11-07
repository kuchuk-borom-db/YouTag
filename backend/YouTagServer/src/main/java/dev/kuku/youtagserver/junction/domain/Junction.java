package dev.kuku.youtagserver.junction.domain;


import dev.kuku.youtagserver.shared.constants.DbConst;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = DbConst.Junction.TABLE_NAME, indexes = {
        @Index(name = "junction_tag", columnList = DbConst.Junction.TAG)
})
@IdClass(JunctionId.class)
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
