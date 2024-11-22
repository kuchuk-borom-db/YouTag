package dev.kuku.youtagserver.user_video_tag.application;

import dev.kuku.youtagserver.user_video_tag.api.UserVideoTagDTO;
import dev.kuku.youtagserver.user_video_tag.api.UserVideoTagService;
import dev.kuku.youtagserver.user_video_tag.domain.UserVideoTag;
import dev.kuku.youtagserver.user_video_tag.infrastructure.UserVideoTagRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Transactional
@RequiredArgsConstructor
@Slf4j
@Service
public class UserVideoTagServiceImpl implements UserVideoTagService {
    final UserVideoTagRepo repo;

    @Override
    public UserVideoTagDTO toDto(UserVideoTag e) {
        return new UserVideoTagDTO(e.getUserId(), e.getVideoId(), e.getTag());
    }
}
