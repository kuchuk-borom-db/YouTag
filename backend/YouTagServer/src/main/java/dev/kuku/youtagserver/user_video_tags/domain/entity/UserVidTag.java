package dev.kuku.youtagserver.user_video_tags.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_video_tags")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@IdClass(UserVidTagId.class)  // This is required for composite key
public class UserVidTag {
    @Id
    @Column(name = "user_email", length = 100)
    private String userEmail;

    @Id
    @Column(name = "video_id", length = 50)
    private String videoId;

    @Column(name = "tags", columnDefinition = "VARCHAR(50)[]")
    @Setter
    private List<String> tags = new ArrayList<>();
}