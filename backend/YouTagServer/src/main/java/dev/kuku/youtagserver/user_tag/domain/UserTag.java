package dev.kuku.youtagserver.user_tag.domain;

import dev.kuku.youtagserver.shared.api.constants.DbConst;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = DbConst.UserTag.TABLE_NAME)
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@IdClass(UserTagId.class)
public class UserTag {
    @Id
    @Column(name = DbConst.CommonColumn.USER_ID)
    String userId;
    @Id
    @Column(name = DbConst.UserTag.TAG)
    String tag;
}
