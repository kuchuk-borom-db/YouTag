package dev.kuku.youtagserver.user.application.eventListener;

import dev.kuku.youtagserver.auth.api.events.GotUserFromTokenEvent;
import dev.kuku.youtagserver.user.api.dto.UserDTO;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user.api.exceptions.InvalidUser;
import dev.kuku.youtagserver.user.api.exceptions.UserAlreadyExists;
import dev.kuku.youtagserver.user.application.services.UserServiceInternal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
class AuthEventListenerUser {
    final UserServiceInternal userService;

    @Async
    @TransactionalEventListener
    void on(GotUserFromTokenEvent event) throws InvalidUser {
        log.info("Got userMap from token {}", event.userMap());

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
