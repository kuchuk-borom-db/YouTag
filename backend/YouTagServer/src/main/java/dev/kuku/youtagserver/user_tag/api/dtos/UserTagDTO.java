package dev.kuku.youtagserver.user_tag.api.dtos;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class UserTagDTO {
    final String userId;
    final String tag;
}
