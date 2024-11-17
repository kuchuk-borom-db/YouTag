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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    final UserRepo userRepo;
    final ApplicationEventPublisher eventPublisher;
    private final Map<String, UserDTO> cache = new ConcurrentHashMap<>();

    /**
     * Generates a cache key for a user based on their email.
     * Since email is unique identifier for users, we can use it directly as the cache key.
     */
    private String generateCacheKey(String email) {
        return "user:" + email;
    }

    /**
     * Evicts a specific user from the cache.
     * Called before modifications to ensure stale data is removed.
     */
    private void evictCache(String email) {
        String cacheKey = generateCacheKey(email);
        cache.remove(cacheKey);
        log.debug("Evicted cache entry for user {}", email);
    }

    @Override
    public UserDTO getUser(String email) throws EmailNotFound, UserDTOHasNullValues {
        log.info("Get user with email {}", email);
        String cacheKey = generateCacheKey(email);

        // Try to get from cache first
        UserDTO cachedUser = cache.get(cacheKey);
        if (cachedUser != null) {
            log.debug("Cache hit for user {}", email);
            return cachedUser;
        }

        // If not in cache, get from database and cache it
        var user = userRepo.findByEmail(email).orElse(null);
        if (user == null) {
            throw new EmailNotFound(email);
        }

        UserDTO userDTO = toDTO(user);
        cache.put(cacheKey, userDTO);
        return userDTO;
    }

    @Override
    public void addUser(String email, String name, String thumbnail) throws InvalidUser, UserAlreadyExists {
        log.debug("Add user {} {} {}", email, name, thumbnail);
        User user = new User(email, name, thumbnail, LocalDateTime.now());
        if (user.getUsername() == null || user.getThumbUrl() == null || user.getEmail() == null) {
            log.error("Invalid user");
            throw new InvalidUser();
        }
        try {
            getUser(user.getEmail());
            log.error("User already exists");
            throw new UserAlreadyExists(email);
        } catch (EmailNotFound e) {
            User savedUser = userRepo.save(user);
            log.info("Saved user {}", savedUser);
            evictCache(email); // Evict any existing cache entry
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
        evictCache(user.getEmail()); // Evict cached entry after update
        eventPublisher.publishEvent(new UserUpdatedEvent(user));
    }

    @Override
    public void deleteUser(String email) throws EmailNotFound, UserDTOHasNullValues {
        getUser(email);
        log.info("Delete user {}", email);
        userRepo.deleteById(email);
        evictCache(email); // Evict cached entry after deletion
        eventPublisher.publishEvent(new UserDeletedEvent(email));
    }

    @Override
    public boolean isUserOutdated(UserDTO userDTO) {
        log.info("Check user outdated {}", userDTO);
        User dbUser = userRepo.findByEmail(userDTO.email()).orElse(null);
        if (dbUser == null) {
            log.error("User not found. Aborting update");
            return false;
        }
        return !dbUser.getEmail().equals(userDTO.email())
                || !dbUser.getUsername().equals(userDTO.name())
                || !dbUser.getThumbUrl().equals(userDTO.pic());
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(user.getEmail(), user.getUsername(), user.getThumbUrl(), user.getUpdated());
    }

    private User toEntity(UserDTO userDTO) {
        return new User(userDTO.email(), userDTO.name(), userDTO.pic(), userDTO.created());
    }
}