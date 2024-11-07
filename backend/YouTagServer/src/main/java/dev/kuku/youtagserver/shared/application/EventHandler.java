package dev.kuku.youtagserver.shared.application;

import dev.kuku.youtagserver.auth.api.events.GotUserFromTokenEvent;
import dev.kuku.youtagserver.user.api.dto.UserDTO;
import dev.kuku.youtagserver.user.api.events.UserAddedEvent;
import dev.kuku.youtagserver.user.api.events.UserDeletedEvent;
import dev.kuku.youtagserver.user.api.events.UserUpdatedEvent;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user.api.exceptions.InvalidUser;
import dev.kuku.youtagserver.user.api.exceptions.UserAlreadyExists;
import dev.kuku.youtagserver.user.api.exceptions.UserDTOHasNullValues;
import dev.kuku.youtagserver.user.api.services.UserService;
import dev.kuku.youtagserver.video.api.events.VideoAddedEvent;
import dev.kuku.youtagserver.video.api.events.VideoDeletedEvent;
import dev.kuku.youtagserver.video.api.events.VideoUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
class AuthEventHandler {
    private final UserService userService;

    /**
     * Tasks :- <br>
     * 1. Update user if not updated. If updated, then evict cache.
     */
    @Async
    @TransactionalEventListener
    void on(GotUserFromTokenEvent event) throws InvalidUser, UserDTOHasNullValues {
        log.info("Got user from token : {}", event);

        String email = event.userMap().get("email");
        String name = event.userMap().get("name");
        String pic = event.userMap().get("picture");
        UserDTO tokenUser = new UserDTO(email, name, pic, LocalDateTime.now());

        try {
            //Attempt to getUserVideoTagByVideoIdUserIdAndTag user
            var existingUser = userService.getUser(email);
            if (userService.isUserOutdated(tokenUser)) {
                log.info("Existing user found {}. But outdated {}", existingUser, tokenUser);
                userService.updateUser(tokenUser);
            }
        } catch (EmailNotFound e) {
            log.info("Email not found. Adding user to database");
            try {
                userService.addUser(tokenUser);
            } catch (UserAlreadyExists ex) {
                log.error("This should not have happened");
            }
        }
    }
}

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
class UserEventHandler {

    /**
     * Tasks :- <br>
     * 1.
     */
    @Async
    @TransactionalEventListener
    void on(UserAddedEvent event) {
        log.info("User added: {}", event);
    }

    /**
     * Tasks :- <br>
     * 1. Clear user from cache
     */
    @Async
    @TransactionalEventListener
    void on(UserUpdatedEvent event) {
        log.info("User updated: {}", event);
        //TODO Clear cache
    }

    /**
     * Tasks :- <br>
     * 2. Remove records from UserVideoTag table where userId matches
     * 3. Clear user from cache
     */
    @Async
    @TransactionalEventListener
    void on(UserDeletedEvent event) {
        log.info("User deleted: {}", event);
    }
}

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
class VideoEventHandler {
    @Async
    @TransactionalEventListener
    void on(VideoAddedEvent event) {
        log.debug("Video added event : {}", event);
    }

    @Async
    @TransactionalEventListener
    void on(VideoUpdatedEvent event) {
        log.debug("Video updated event : {}", event);
    }

    @Async
    @TransactionalEventListener
    void on(VideoDeletedEvent event) {
        log.debug("Video deleted event : {}", event);
    }
}