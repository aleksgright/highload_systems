import org.itmo.user.accounter.model.entities.User;
import org.itmo.user.accounter.repositories.UserRepository;
import org.itmo.user.accounter.services.UserService;
import org.itmo.user.accounter.utils.exceptions.DataIntegrityViolationException;
import org.itmo.user.accounter.utils.exceptions.ItemNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final UserService userService = new UserService(userRepository);

    private User testUser;
    private User existingUser;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "TestUser");
        existingUser = new User(2L, "ExistingUser");
    }

    @Test
    void save_ShouldSaveUser_WhenUserDoesNotExist() {
        when(userRepository.findByName(testUser.getName())).thenReturn(Mono.empty());
        when(userRepository.save(testUser)).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.save(testUser))
                .expectNextMatches(user ->
                        user.getId().equals(1L) &&
                                user.getName().equals("TestUser")
                )
                .verifyComplete();

        verify(userRepository).findByName(testUser.getName());
        verify(userRepository).save(testUser);
    }

    @Test
    void save_ShouldFail_WhenUserWithSameNameExists() {
        when(userRepository.findByName(testUser.getName()))
                .thenReturn(Mono.just(existingUser));

        StepVerifier.create(userService.save(testUser))
                .expectError(DataIntegrityViolationException.class)
                .verify();

        verify(userRepository).findByName(testUser.getName());
        verify(userRepository, never()).save(any());
    }

    @Test
    void update_ShouldUpdate_WhenUserExists() {
        when(userRepository.findById(testUser.getId()))
                .thenReturn(Mono.just(existingUser));
        when(userRepository.save(any()))
                .thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.update(testUser))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void update_ShouldFail_WhenUserNotFound() {
        when(userRepository.findById(testUser.getId()))
                .thenReturn(Mono.empty());

        StepVerifier.create(userService.update(testUser))
                .expectError(ItemNotFoundException.class)
                .verify();
    }

    @Test
    void delete_ShouldComplete_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));
        when(userRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.deleteById(1L))
                .verifyComplete();
    }

    @Test
    void delete_ShouldFail_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.deleteById(1L))
                .expectError(ItemNotFoundException.class)
                .verify();
    }

    @Test
    void findById_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.findById(1L))
                .expectNext(testUser)
                .verifyComplete();
    }

    @Test
    void findById_ShouldReturnEmpty() {
        when(userRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.findById(1L))
                .verifyComplete();
    }

    @Test
    void findByName_ShouldReturnUser() {
        when(userRepository.findByName("TestUser"))
                .thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.findByName("TestUser"))
                .expectNext(testUser)
                .verifyComplete();
    }

    @Test
    void findByName_ShouldReturnEmpty() {
        when(userRepository.findByName("Unknown"))
                .thenReturn(Mono.empty());

        StepVerifier.create(userService.findByName("Unknown"))
                .verifyComplete();
    }
}
