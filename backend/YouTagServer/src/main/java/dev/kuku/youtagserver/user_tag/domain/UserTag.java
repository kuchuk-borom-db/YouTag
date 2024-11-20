package dev.kuku.youtagserver.user_tag.domain;


import dev.kuku.youtagserver.shared.constants.DbConst;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = DbConst.UserTag.TABLE_NAME)
@ToString
public class UserTag {
    @Id
    @Column(name = DbConst.CommonColumn.ID)
    String id;
    @Column(name = DbConst.UserTag.USER_ID)
    String userId;
    @Column(name = DbConst.UserTag.TAG)
    String tag;
}
