package dev.kuku.youtagserver.user_tag.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserTagId implements Serializable {
    String userId;
    String tag;
}
