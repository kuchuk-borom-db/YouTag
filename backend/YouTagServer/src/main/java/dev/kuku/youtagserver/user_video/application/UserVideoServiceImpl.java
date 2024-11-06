package dev.kuku.youtagserver.user_video.application;

import dev.kuku.youtagserver.user_video.api.dto.UserVideoDTO;
import dev.kuku.youtagserver.user_video.api.exception.VideoAlreadyLinkedToUser;
import dev.kuku.youtagserver.user_video.api.services.UserVideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserVideoServiceImpl implements UserVideoService {

    @Override
    public void create(String videoId, String currentUserId) throws VideoAlreadyLinkedToUser {

    }

    @Override
    public UserVideoDTO get(String userId, String videoId) {
        return null;
    }

    @Override
    public List<UserVideoDTO> getWithUserId(String userId) {
        return List.of();
    }

    @Override
    public List<UserVideoDTO> getWithVideoId(String videoId) {
        return List.of();
    }

    @Override
    public void delete(String userId, String videoId) {

    }

    @Override
    public void deleteWithUserId(String userId) {

    }

    @Override
    public void deleteWithVideoId(String videoId) {

    }
}
