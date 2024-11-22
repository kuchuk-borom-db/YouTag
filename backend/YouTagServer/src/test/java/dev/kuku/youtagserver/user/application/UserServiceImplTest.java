package dev.kuku.youtagserver.user.application;

import dev.kuku.youtagserver.user.api.dto.UserDTO;
import dev.kuku.youtagserver.user.api.events.UserAddedEvent;
import dev.kuku.youtagserver.user.api.events.UserUpdatedEvent;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user.api.exceptions.InvalidUser;
import dev.kuku.youtagserver.user.api.exceptions.UserAlreadyExists;
import dev.kuku.youtagserver.user.api.exceptions.UserDTOHasNullValues;
import dev.kuku.youtagserver.user.domain.User;
import dev.kuku.youtagserver.user.infrastructure.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private UserServiceImpl userService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_NAME = "Test User";
    private static final String TEST_THUMBNAIL = "http://example.com/thumb.jpg";
    private static final LocalDateTime TEST_DATE = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepo, eventPublisher);
    }

    @Test
    void getUser_WhenUserExists_ReturnsUserDTO() throws EmailNotFound, UserDTOHasNullValues {
        // Arrange
        User user = new User(TEST_EMAIL, TEST_NAME, TEST_THUMBNAIL, TEST_DATE);
        when(userRepo.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));

        // Act
        UserDTO result = userService.getUser(TEST_EMAIL);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_EMAIL, result.email());
        assertEquals(TEST_NAME, result.name());
        assertEquals(TEST_THUMBNAIL, result.pic());
    }

    @Test
    void getUser_WhenUserDoesNotExist_ThrowsEmailNotFound() {
        // Arrange
        when(userRepo.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EmailNotFound.class, () -> userService.getUser(TEST_EMAIL));
    }

    @Test
    void getUser_WhenCached_ReturnsCachedValue() throws EmailNotFound, UserDTOHasNullValues {
        // Arrange
        User user = new User(TEST_EMAIL, TEST_NAME, TEST_THUMBNAIL, TEST_DATE);
        when(userRepo.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));

        // Act
        UserDTO firstCall = userService.getUser(TEST_EMAIL);
        UserDTO secondCall = userService.getUser(TEST_EMAIL);

        // Assert
        assertEquals(firstCall, secondCall);
        verify(userRepo, times(1)).findByEmail(TEST_EMAIL); // Repository should only be called once
    }

    @Test
    void addUser_WhenUserDoesNotExist_SavesAndPublishesEvent() throws InvalidUser, UserAlreadyExists {
        // Arrange
        User user = new User(TEST_EMAIL, TEST_NAME, TEST_THUMBNAIL, TEST_DATE);
        when(userRepo.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(userRepo.save(any(User.class))).thenReturn(user);

        // Act
        userService.addUser(TEST_EMAIL, TEST_NAME, TEST_THUMBNAIL);

        // Assert
        verify(userRepo).save(any(User.class));
        verify(eventPublisher).publishEvent(any(UserAddedEvent.class));
    }

    @Test
    void addUser_WhenUserExists_ThrowsUserAlreadyExists() {
        // Arrange
        User existingUser = new User(TEST_EMAIL, TEST_NAME, TEST_THUMBNAIL, TEST_DATE);
        when(userRepo.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThrows(UserAlreadyExists.class,
                () -> userService.addUser(TEST_EMAIL, TEST_NAME, TEST_THUMBNAIL));
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void updateUser_WhenUserIsOutdated_UpdatesAndPublishesEvent() {
        // Arrange
        User existingUser = new User(TEST_EMAIL, "Old Name", "old-thumb.jpg", TEST_DATE);
        UserDTO updateDTO = new UserDTO(TEST_EMAIL, TEST_NAME, TEST_THUMBNAIL, TEST_DATE);
        when(userRepo.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(existingUser));
        when(userRepo.save(any(User.class))).thenReturn(existingUser);

        // Act
        userService.updateUser(updateDTO);

        // Assert
        verify(userRepo).save(any(User.class));
        verify(eventPublisher).publishEvent(any(UserUpdatedEvent.class));
    }

    @Test
    void updateUser_WhenUserIsNotOutdated_DoesNotUpdate() {
        // Arrange
        User existingUser = new User(TEST_EMAIL, TEST_NAME, TEST_THUMBNAIL, TEST_DATE);
        UserDTO updateDTO = new UserDTO(TEST_EMAIL, TEST_NAME, TEST_THUMBNAIL, TEST_DATE);
        when(userRepo.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(existingUser));

        // Act
        userService.updateUser(updateDTO);

        // Assert
        verify(userRepo, never()).save(any(User.class));
        verify(eventPublisher, never()).publishEvent(any(UserUpdatedEvent.class));
    }

    @Test
    void deleteUser_WhenUserExists_DeletesAndPublishesEvent() throws UserDTOHasNullValues, EmailNotFound {
        // Arrange
        User existingUser = new User(TEST_EMAIL, TEST_NAME, TEST_THUMBNAIL, TEST_DATE);
        when(userRepo.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(existingUser));

        // Act
        userService.deleteUser(TEST_EMAIL);

        // Assert
        verify(userRepo).deleteById(TEST_EMAIL);

        ArgumentCaptor<UserDeletedEvent> eventCaptor = ArgumentCaptor.forClass(UserDeletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(TEST_EMAIL, eventCaptor.getValue().userId());
    }

    @Test
    void isUserOutdated_ReturnsTrueWhenUserDifferent() {
        // Arrange
        User existingUser = new User(TEST_EMAIL, "Old Name", "old-thumb.jpg", TEST_DATE);
        UserDTO updateDTO = new UserDTO(TEST_EMAIL, TEST_NAME, TEST_THUMBNAIL, TEST_DATE);
        when(userRepo.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(existingUser));

        // Act
        boolean result = userService.isUserOutdated(updateDTO);

        // Assert
        assertTrue(result);
    }

    @Test
    void isUserOutdated_ReturnsFalseWhenUserSame() {
        // Arrange
        User existingUser = new User(TEST_EMAIL, TEST_NAME, TEST_THUMBNAIL, TEST_DATE);
        UserDTO updateDTO = new UserDTO(TEST_EMAIL, TEST_NAME, TEST_THUMBNAIL, TEST_DATE);
        when(userRepo.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(existingUser));

        // Act
        boolean result = userService.isUserOutdated(updateDTO);

        // Assert
        assertFalse(result);
    }
}