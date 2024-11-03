package dev.kuku.youtagserver.user.application.services;

import dev.kuku.youtagserver.user.api.events.UserAddedEvent;
import dev.kuku.youtagserver.user.api.events.UserUpdatedEvent;
import dev.kuku.youtagserver.user.api.services.UserService;
import dev.kuku.youtagserver.user.domain.entity.User;
import dev.kuku.youtagserver.user.infrastructure.persistence.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    final UserRepo userRepo;
    final ApplicationEventPublisher eventPublisher;

    @Override
    public User getUser(String email) {
        log.info("Get user with email {}", email);
        Optional<User> user = userRepo.findByEmail(email);
        if (user.isPresent()) {
            return user.get();
        }
        log.error("User not found");
        return null;
    }

    @Override
    public boolean addUser(User user) {
        log.info("Add user {}", user);
        if (user == null || user.getUsername() == null || user.getPic() == null || user.getEmail() == null) {
            log.error("Invalid user");
            return false;
        }
        if (getUser(user.getEmail()) != null) {
            log.error("User already exists");
            return false;
        }
        User savedUser = userRepo.save(user);
        eventPublisher.publishEvent(new UserAddedEvent(savedUser));
        return true;
    }

    @Override
    public boolean updateUser(User user) {
        log.info("Update user {}", user);
        if (!isUserOutdated(user)) {
            log.error("User is not outdated");
            return false;
        }
        userRepo.save(user);
        eventPublisher.publishEvent(new UserUpdatedEvent(user));
        return true;
    }

    @Override
    public boolean isUserOutdated(User user) {
        log.info("Check user outdated {}", user);
        User dbUser = getUser(user.getEmail());
        if (dbUser == null) {
            log.error("User not found. Aborting update");
            return false;
        }
        return !dbUser.getEmail().equals(user.getEmail()) || !dbUser.getUsername().equals(user.getUsername()) || !dbUser.getPic().equals(user.getPic());
    }
}
