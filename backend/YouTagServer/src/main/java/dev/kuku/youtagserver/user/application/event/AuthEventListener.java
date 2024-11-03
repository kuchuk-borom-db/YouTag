package dev.kuku.youtagserver.user.application.event;

import dev.kuku.youtagserver.auth.api.events.GotUserFromTokenEvent;
import dev.kuku.youtagserver.user.api.services.UserService;
import dev.kuku.youtagserver.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@Slf4j
@RequiredArgsConstructor
class AuthEventListener {
    final UserService userService;

    @Async
    @TransactionalEventListener
    void on(GotUserFromTokenEvent event) {
        log.info("Got userMap from token {}", event.userMap());

        String email = event.userMap().get("email");
        String name = event.userMap().get("name");
        String pic = event.userMap().get("picture");
        User tokenUser = new User(email, name, pic);
        //if user already then attempt to update it
        if (!userService.addUser(tokenUser)) {
            if (userService.updateUser(tokenUser)) {
                log.info("Updating user {}", tokenUser);
            } else {
                log.info("Found existing updated user.");
            }
        } else {
            log.info("Added new user {}", tokenUser);
        }


    }
}
