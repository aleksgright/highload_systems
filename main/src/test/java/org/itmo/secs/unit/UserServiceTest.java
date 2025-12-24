package org.itmo.secs.unit;

import org.itmo.secs.model.entities.User;
import org.itmo.secs.repositories.UserRepository;
import org.itmo.secs.services.UserService;
import org.itmo.secs.utils.exceptions.DataIntegrityViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
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
        when(userRepository.findByName(testUser.getName())).thenReturn(Optional.empty());
        when(userRepository.save(testUser)).thenReturn(testUser);

        User savedUser = userService.save(testUser);

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
    void save_ShouldHandleNullUserName() {
        // Arrange
        User userWithNullName = new User();
        userWithNullName.setId(1L);
        userWithNullName.setName(null);

        when(userRepository.findByName(null)).thenReturn(Optional.empty());
        when(userRepository.save(userWithNullName)).thenReturn(userWithNullName);

        // Act & Assert
        assertDoesNotThrow(() -> userService.save(userWithNullName));

        verify(userRepository).findByName(null);
        verify(userRepository).save(userWithNullName);
    }

    @Test
    void save_ShouldHandleEmptyUserName() {
        // Arrange
        User userWithEmptyName = new User();
        userWithEmptyName.setId(1L);
        userWithEmptyName.setName("");

        when(userRepository.findByName("")).thenReturn(Optional.empty());
        when(userRepository.save(userWithEmptyName)).thenReturn(userWithEmptyName);

        // Act & Assert
        assertDoesNotThrow(() -> userService.save(userWithEmptyName));

        verify(userRepository).findByName("");
        verify(userRepository).save(userWithEmptyName);
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
        when(userRepository.findByName(null)).thenReturn(Optional.empty());

        User foundUser = userService.findByName(null);

        assertNull(foundUser);

        verify(userRepository).findByName(null);
    }

    @Test
    void findByName_ShouldHandleEmptyName() {
        when(userRepository.findByName("")).thenReturn(Optional.empty());

        User foundUser = userService.findByName("");

        assertNull(foundUser);

        verify(userRepository).findByName("");
    }

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User foundUser = userService.findById(1L);

        assertNotNull(foundUser);
        assertEquals(testUser.getId(), foundUser.getId());
        assertEquals(testUser.getName(), foundUser.getName());

        verify(userRepository).findById(1L);
    }

    @Test
    void findById_ShouldReturnNull_WhenUserDoesNotExist() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        User foundUser = userService.findById(999L);

        assertNull(foundUser);

        verify(userRepository).findById(999L);
    }

    @Test
    void findById_ShouldHandleNullId() {
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        User foundUser = userService.findById(null);

        assertNull(foundUser);

        verify(userRepository).findById(null);
    }

    @Test
    void findById_ShouldHandleZeroId() {
        when(userRepository.findById(0L)).thenReturn(Optional.empty());

        User foundUser = userService.findById(0L);

        assertNull(foundUser);

        verify(userRepository).findById(0L);
    }
}