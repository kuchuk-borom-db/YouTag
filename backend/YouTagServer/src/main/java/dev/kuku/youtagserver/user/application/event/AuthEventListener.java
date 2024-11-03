package dev.kuku.youtagserver.user.application.event;

import dev.kuku.youtagserver.auth.api.events.GotUserFromTokenEvent;
import dev.kuku.youtagserver.user.domain.entity.User;
import dev.kuku.youtagserver.user.api.events.UserAddedEvent;
import dev.kuku.youtagserver.user.api.events.UserUpdatedEvent;
import dev.kuku.youtagserver.user.infrastructure.persistence.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
class AuthEventListener {
    final UserRepo userRepo;
    final ApplicationEventPublisher eventPublisher;

    @Async
    @TransactionalEventListener
    void on(GotUserFromTokenEvent event) {
        log.info("Got userMap from token {}", event.userMap());

        String email = event.userMap().get("email");
        String name = event.userMap().get("name");
        String pic = event.userMap().get("picture");

        // Check if user already exists in the database
        Optional<User> existingUserOpt = userRepo.findByEmail(email);

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            // Check if existing user's information is outdated
            boolean isOutdated = !existingUser.getUsername().equals(name) || !existingUser.getPic().equals(pic);

            if (isOutdated) {
                // Update the existing user's information
                existingUser.setUsername(name);
                existingUser.setPic(pic);
                userRepo.save(existingUser);  // Save updated user to database
                log.info("Updated user with email: {}", email);
                eventPublisher.publishEvent(new UserUpdatedEvent(existingUser));
            } else {
                log.info("User with email: {} is up-to-date. No update needed.", email);
            }
        } else {
            // Create a new user if one does not already exist
            User newUser = new User(email, name, pic);
            userRepo.save(newUser);  // Save new user to database
            log.info("Created new user with email: {}", email);
            eventPublisher.publishEvent(new UserAddedEvent(newUser));
        }
    }
}
