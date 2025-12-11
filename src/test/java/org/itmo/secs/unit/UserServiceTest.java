package org.itmo.secs.unit;

import org.itmo.secs.model.entities.User;
import org.itmo.secs.repositories.ItemRepository;
import org.itmo.secs.repositories.UserRepository;
import org.itmo.secs.services.UserService;
import org.itmo.secs.utils.exceptions.DataIntegrityViolationException;
import org.itmo.secs.utils.exceptions.ItemNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);;

    @Autowired
    private final UserService userService = new UserService(userRepository);

    private User testUser;
    private User existingUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("TestUser");

        existingUser = new User();
        existingUser.setId(2L);
        existingUser.setName("ExistingUser");
    }

    @Test
    void save_ShouldSaveUser_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByName(testUser.getName())).thenReturn(Optional.empty());
        when(userRepository.save(testUser)).thenReturn(testUser);

        // Act
        User savedUser = userService.save(testUser);

        // Assert
        assertNotNull(savedUser);
        assertEquals(testUser.getId(), savedUser.getId());
        assertEquals(testUser.getName(), savedUser.getName());

        verify(userRepository).findByName(testUser.getName());
        verify(userRepository).save(testUser);
    }

    @Test
    void save_ShouldThrowDataIntegrityViolationException_WhenUserWithSameNameExists() {
        // Arrange
        when(userRepository.findByName(testUser.getName())).thenReturn(Optional.of(existingUser));

        // Act & Assert
        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> userService.save(testUser)
        );

        assertEquals("User with name " + testUser.getName() + " already exist", exception.getMessage());

        verify(userRepository).findByName(testUser.getName());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void save_ShouldHandleNullUserGracefully() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> userService.save(null));
    }

    @Test
    void update_ShouldUpdateUser_WhenUserExists() {
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("UpdatedName");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        userService.update(updatedUser);

        verify(userRepository).findById(1L);
        verify(userRepository).save(argThat(user ->
                user.getId().equals(1L) &&
                        user.getName().equals("UpdatedName")
        ));
    }

    @Test
    void update_ShouldThrowItemNotFoundException_WhenUserDoesNotExist() {
        User nonExistentUser = new User();
        nonExistentUser.setId(999L);
        nonExistentUser.setName("NonExistent");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> userService.update(nonExistentUser)
        );

        assertEquals("User with id 999 was not found", exception.getMessage());

        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_ShouldHandleNullUser() {
        assertThrows(NullPointerException.class, () -> userService.update(null));
    }

    @Test
    void findByName_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findByName("ExistingUser")).thenReturn(Optional.of(existingUser));

        User foundUser = userService.findByName("ExistingUser");

        assertNotNull(foundUser);
        assertEquals(existingUser.getId(), foundUser.getId());
        assertEquals(existingUser.getName(), foundUser.getName());

        verify(userRepository).findByName("ExistingUser");
    }

    @Test
    void findByName_ShouldReturnNull_WhenUserDoesNotExist() {
        when(userRepository.findByName("NonExistent")).thenReturn(Optional.empty());

        User foundUser = userService.findByName("NonExistent");

        assertNull(foundUser);

        verify(userRepository).findByName("NonExistent");
    }

    @Test
    void findByName_ShouldHandleNullName() {
        User foundUser = userService.findByName(null);

        assertNull(foundUser);

        verify(userRepository).findByName(null);
    }

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User foundUser = userService.findById(1L);

        // Assert
        assertNotNull(foundUser);
        assertEquals(testUser.getId(), foundUser.getId());
        assertEquals(testUser.getName(), foundUser.getName());

        verify(userRepository).findById(1L);
    }

    @Test
    void findById_ShouldReturnNull_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        User foundUser = userService.findById(999L);

        // Assert
        assertNull(foundUser);

        verify(userRepository).findById(999L);
    }

    @Test
    void findById_ShouldHandleNullId() {
        // Act
        User foundUser = userService.findById(null);

        // Assert
        assertNull(foundUser);

        verify(userRepository).findById(null);
    }

    @Test
    void findById_ShouldHandleZeroId() {
        // Arrange
        when(userRepository.findById(0L)).thenReturn(Optional.empty());

        // Act
        User foundUser = userService.findById(0L);

        // Assert
        assertNull(foundUser);

        verify(userRepository).findById(0L);
    }

    @Test
    void update_ShouldOnlyUpdateName_WhenUserExists() {
        // Arrange
        User originalUser = new User();
        originalUser.setId(1L);
        originalUser.setName("OriginalName");

        User updateRequest = new User();
        updateRequest.setId(1L);
        updateRequest.setName("NewName");
        // Note: email and password are not set in updateRequest

        when(userRepository.findById(1L)).thenReturn(Optional.of(originalUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userService.update(updateRequest);

        // Assert
        verify(userRepository).save(argThat(savedUser ->
                savedUser.getId().equals(1L) &&
                        savedUser.getName().equals("NewName")
        ));
    }

    @Test
    void save_ShouldThrowException_WhenUserNameIsNull() {
        // Arrange
        User userWithNullName = new User();
        userWithNullName.setId(1L);
        userWithNullName.setName(null);

        when(userRepository.findByName(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertDoesNotThrow(() -> userService.save(userWithNullName));

        verify(userRepository).findByName(null);
        verify(userRepository).save(userWithNullName);
    }
}