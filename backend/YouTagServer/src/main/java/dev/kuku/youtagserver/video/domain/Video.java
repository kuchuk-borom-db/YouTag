package dev.kuku.youtagserver.video.domain;

import dev.kuku.youtagserver.shared.constants.DbConst;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Entity
@Table(name = DbConst.Videos.TABLE_NAME)
public class Video {
    @Id
    @Column(name = DbConst.CommonColumn.ID, length = 50)
    String id;
    @Column(name = DbConst.Videos.TITLE, length = 250)
    String title;
    @Column(name = DbConst.Videos.DESCRIPTION, length = 500)
    String description;
    @Column(name = DbConst.Videos.THUMBNAIL_URL, length = 250)
    String thumbnail;
    @Column(name = DbConst.CommonColumn.UPDATED, nullable = false)
    LocalDateTime updated;
}
