package dev.kuku.youtagserver.user.domain.entity;

import dev.kuku.youtagserver.shared.constants.DbConst;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Table(name = DbConst.Users.TABLE_NAME)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User {
    @Id
    @Column(name = DbConst.CommonColumn.ID, unique = true, nullable = false, length = 250)
    String email;
    @Column(name = DbConst.Users.USERNAME, nullable = false, length = 250)
    String username;
    @Column(name = DbConst.Users.THUMBNAIL_URL, nullable = false, length = 250)
    String thumbUrl;
    @Column(name = DbConst.CommonColumn.UPDATED, nullable = false)
    LocalDateTime updated;
}

