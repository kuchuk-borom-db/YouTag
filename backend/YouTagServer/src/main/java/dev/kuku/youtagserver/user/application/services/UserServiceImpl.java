package dev.kuku.youtagserver.user.application.services;

import dev.kuku.youtagserver.shared.helper.CacheSystem;
import dev.kuku.youtagserver.user.api.dto.UserDTO;
import dev.kuku.youtagserver.user.api.events.UserAddedEvent;
import dev.kuku.youtagserver.user.api.events.UserUpdatedEvent;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user.domain.entity.User;
import dev.kuku.youtagserver.user.infrastructure.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserServiceInternal {
    final UserRepo userRepo;
    final ApplicationEventPublisher eventPublisher;
    final CacheSystem cacheSystem;

    @Override
    public UserDTO getUser(String email) throws EmailNotFound {
        log.info("Get user with email {}", email);
        User user = (User) cacheSystem.getObject("user", email);
        if (user == null) {
            user = userRepo.findByEmail(email).orElse(null);
            cacheSystem.cache("user", email, user);
        }
        if (user == null) {
            throw new EmailNotFound(email);
        }
        return toDTO(user);
    }

    @Override
    public boolean addUser(UserDTO userDTO) {
        log.info("Add user {}", userDTO);
        User user = toEntity(userDTO);
        if (user.getUsername() == null || user.getThumbUrl() == null || user.getEmail() == null) {
            log.error("Invalid user");
            return false;
        }
        try {
            getUser(user.getEmail());
            log.error("User already exists");
            return false;
        } catch (EmailNotFound e) {
            User savedUser = userRepo.save(user);
            eventPublisher.publishEvent(new UserAddedEvent(savedUser));
            return true;
        }

    }

    @Override
    public boolean updateUser(UserDTO userDTO) {
        log.info("Update user {}", userDTO);
        User user = toEntity(userDTO);
        if (!isUserOutdated(userDTO)) {
            log.error("User is not outdated");
            return false;
        }
        cacheSystem.evict("user", userDTO.email());
        user.setUpdated(LocalDateTime.now());
        userRepo.save(user);
        eventPublisher.publishEvent(new UserUpdatedEvent(user));
        return true;
    }

    @Override
    public boolean isUserOutdated(UserDTO userDTO) {
        log.info("Check user outdated {}", userDTO);
        User dbUser = userRepo.findByEmail(userDTO.email()).orElse(null);
        if (dbUser == null) {
            log.error("User not found. Aborting update");
            return false;
        }
        return !dbUser.getEmail().equals(userDTO.email()) || !dbUser.getUsername().equals(userDTO.name()) || !dbUser.getThumbUrl().equals(userDTO.pic());
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(user.getEmail(), user.getUsername(), user.getThumbUrl(), user.getUpdated());
    }

    private User toEntity(UserDTO userDTO) {
        return new User(userDTO.email(), userDTO.name(), userDTO.pic(), userDTO.created());
    }
}