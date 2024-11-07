package dev.kuku.youtagserver.user.application;

import dev.kuku.youtagserver.user.api.dto.UserDTO;
import dev.kuku.youtagserver.user.api.events.UserAddedEvent;
import dev.kuku.youtagserver.user.api.events.UserDeletedEvent;
import dev.kuku.youtagserver.user.api.events.UserUpdatedEvent;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user.api.exceptions.InvalidUser;
import dev.kuku.youtagserver.user.api.exceptions.UserAlreadyExists;
import dev.kuku.youtagserver.user.api.exceptions.UserDTOHasNullValues;
import dev.kuku.youtagserver.user.api.services.UserService;
import dev.kuku.youtagserver.user.domain.User;
import dev.kuku.youtagserver.user.infrastructure.UserRepo;
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
public class UserServiceImpl implements UserService {
    final UserRepo userRepo;
    final ApplicationEventPublisher eventPublisher;

    @Override
    public UserDTO getUser(String email) throws EmailNotFound, UserDTOHasNullValues {
        log.info("Get user with email {}", email);
        var user = userRepo.findByEmail(email).orElse(null);
        if (user == null) {
            throw new EmailNotFound(email);
        }
        return toDTO(user);
    }

    @Override
    public void addUser(UserDTO userDTO) throws InvalidUser, UserAlreadyExists {
        log.info("Add user {} called", userDTO);
        User user = toEntity(userDTO);
        if (user.getUsername() == null || user.getThumbUrl() == null || user.getEmail() == null) {
            log.error("Invalid user");
            throw new InvalidUser();
        }
        try {
            getUser(user.getEmail());
            log.error("User already exists");
            throw new UserAlreadyExists(userDTO.getEmail());
        } catch (EmailNotFound e) {
            User savedUser = userRepo.save(user);
            log.info("Saved user {}", savedUser);
            eventPublisher.publishEvent(new UserAddedEvent(savedUser));
        } catch (UserDTOHasNullValues e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateUser(UserDTO userDTO) {
        log.info("Update user {}", userDTO);
        User user = toEntity(userDTO);
        if (!isUserOutdated(userDTO)) {
            log.error("User is not outdated");
            return;
        }
        user.setUpdated(LocalDateTime.now());
        userRepo.save(user);
        eventPublisher.publishEvent(new UserUpdatedEvent(user));
    }

    @Override
    public void deleteUser(String email) throws EmailNotFound, UserDTOHasNullValues {
        getUser(email);
        log.info("Delete user {}", email);
        userRepo.deleteById(email);
        eventPublisher.publishEvent(new UserDeletedEvent(email));
    }

    @Override
    public boolean isUserOutdated(UserDTO userDTO) {
        log.info("Check user outdated {}", userDTO);
        User dbUser = userRepo.findByEmail(userDTO.getEmail()).orElse(null);
        if (dbUser == null) {
            log.error("User not found. Aborting update");
            return false;
        }
        return !dbUser.getEmail().equals(userDTO.getEmail()) || !dbUser.getUsername().equals(userDTO.getName()) || !dbUser.getThumbUrl().equals(userDTO.getPic());
    }

    private UserDTO toDTO(User user) throws UserDTOHasNullValues {
        return new UserDTO(user.getEmail(), user.getUsername(), user.getThumbUrl(), user.getUpdated());
    }

    private User toEntity(UserDTO userDTO) {
        return new User(userDTO.getEmail(), userDTO.getName(), userDTO.getPic(), userDTO.getCreated());
    }
}