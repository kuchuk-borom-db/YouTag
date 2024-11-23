package dev.kuku.youtagserver.shared.application;

import dev.kuku.youtagserver.auth.api.events.GotUserFromTokenEvent;
import dev.kuku.youtagserver.user.api.dto.UserDTO;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user.api.exceptions.InvalidUser;
import dev.kuku.youtagserver.user.api.exceptions.UserAlreadyExists;
import dev.kuku.youtagserver.user.api.exceptions.UserDTOHasNullValues;
import dev.kuku.youtagserver.user.api.services.UserService;
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
public class AuthEventListener {
    private final UserService userService;

    /**
     * Extracts the user information from token and then updates existing user or creates a new one
     */
    @Async
    @TransactionalEventListener
    void on(GotUserFromTokenEvent event) throws InvalidUser, UserDTOHasNullValues {
        log.debug("Got user from token : {}", event);

        String email = event.userMap().get("email");
        String name = event.userMap().get("name");
        String pic = event.userMap().get("picture");
        UserDTO tokenUser = new UserDTO(email, name, pic, LocalDateTime.now());

        try {
            //Attempt to getUserVideoTagByVideoIdUserIdAndTag user
            var existingUser = userService.getUser(email);
            if (userService.isUserOutdated(tokenUser)) {
                log.debug("Existing user found {}. But outdated {}", existingUser, tokenUser);
                userService.updateUser(tokenUser);
            } else {
                log.debug("Existing user found with up to date record{}.", existingUser);
            }
        } catch (EmailNotFound e) {
            log.info("Email not found. Adding user to database");
            try {
                userService.addUser(tokenUser.email(), tokenUser.name(), tokenUser.pic());
            } catch (UserAlreadyExists ex) {
                log.error("This should not have happened");
            }
        }
    }
}
