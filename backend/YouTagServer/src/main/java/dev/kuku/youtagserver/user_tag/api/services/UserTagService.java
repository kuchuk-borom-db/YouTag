package dev.kuku.youtagserver.user_tag.api.services;

import dev.kuku.youtagserver.shared.application.Service;
import dev.kuku.youtagserver.user_tag.api.dtos.UserTagDTO;
import dev.kuku.youtagserver.user_tag.domain.UserTag;

import java.util.List;

public interface UserTagService extends Service<UserTag, UserTagDTO> {

    List<String> getAllTagsOfUser(String userId, int skip, int limit);
}
