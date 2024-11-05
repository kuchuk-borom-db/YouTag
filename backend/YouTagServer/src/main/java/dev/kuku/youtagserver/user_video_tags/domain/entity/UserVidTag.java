package dev.kuku.youtagserver.user_video_tags.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
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
    @Convert(converter = StringListConverter.class)
    @Setter
    private List<String> tags = new ArrayList<>();
}

// StringListConverter.java
@Converter
class StringListConverter implements AttributeConverter<List<String>, String> {
    private static final String SPLIT_CHAR = ",";

    @Override
    public String convertToDatabaseColumn(List<String> strings) {
        if (strings == null || strings.isEmpty()) {
            return "{}";
        }
        return "{" + String.join(SPLIT_CHAR, strings) + "}";
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.equals("{}")) {
            return new ArrayList<>();
        }
        String content = dbData.substring(1, dbData.length() - 1);
        if (content.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(content.split(SPLIT_CHAR)));
    }
}
