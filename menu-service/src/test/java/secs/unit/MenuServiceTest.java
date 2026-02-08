package secs.unit;

import org.itmo.secs.client.DishServiceClient;
import org.itmo.secs.client.UserServiceClient;
import org.itmo.secs.model.dto.DishDto;
import org.itmo.secs.model.dto.UserDto;
import org.itmo.secs.model.entities.Menu;
import org.itmo.secs.model.entities.enums.Meal;
import org.itmo.secs.repositories.MenuRepository;
import org.itmo.secs.services.MenuDishesService;
import org.itmo.secs.services.MenuService;
import org.itmo.secs.utils.exceptions.DataIntegrityViolationException;
import org.itmo.secs.utils.exceptions.ItemNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MenuServiceTest {
    private final MenuRepository menuRepository = Mockito.mock(MenuRepository.class);
    private final MenuDishesService menuDishesService = Mockito.mock(MenuDishesService.class);
    private final DishServiceClient dishServiceClient = Mockito.mock(DishServiceClient.class);
    private final UserServiceClient userServiceClient = Mockito.mock(UserServiceClient.class);

    private final MenuService menuService = new MenuService(
            menuRepository,
            menuDishesService,
            dishServiceClient,
            userServiceClient
    );

    private Menu testMenu;
    private UserDto testUserDto;
    private DishDto testDishDto;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2024, 1, 15);

        testUserDto = new UserDto(1L, "TestUser");
        testDishDto = new DishDto(100L, "Test Dish", 100, 20, 10, 5);

        testMenu = new Menu();
        testMenu.setId(1L);
        testMenu.setMeal(Meal.BREAKFAST);
        testMenu.setDate(testDate);
        testMenu.setUserId(1L);
    }

    @Test
    void save_ShouldSaveMenu_WhenMenuDoesNotExistAndUserExists() {
        when(userServiceClient.getById(1L))
                .thenReturn(Mono.just(testUserDto));
        when(menuRepository.findByMealAndDateAndUserId(
                eq(Meal.BREAKFAST), eq(testDate), eq(1L)
        )).thenReturn(Mono.empty());
        when(menuRepository.save(any(Menu.class)))
                .thenReturn(Mono.just(testMenu));

        StepVerifier.create(menuService.save(testMenu))
                .expectNextMatches(savedMenu ->
                        savedMenu.getId().equals(1L) &&
                                savedMenu.getMeal() == Meal.BREAKFAST &&
                                savedMenu.getUserId().equals(1L)
                )
                .verifyComplete();

        verify(userServiceClient).getById(1L);
        verify(menuRepository).findByMealAndDateAndUserId(
                eq(Meal.BREAKFAST), eq(testDate), eq(1L)
        );
        verify(menuRepository).save(any(Menu.class));
    }

    @Test
    void save_ShouldThrowDataIntegrityViolationException_WhenMenuWithSameKeyExists() {
        when(userServiceClient.getById(1L))
                .thenReturn(Mono.just(testUserDto));
        when(menuRepository.findByMealAndDateAndUserId(
                eq(Meal.BREAKFAST), eq(testDate), eq(1L)
        )).thenReturn(Mono.just(testMenu));
        when(menuRepository.save(any(Menu.class)))
                .thenReturn(Mono.just(testMenu));

        StepVerifier.create(menuService.save(testMenu))
                .expectErrorMatches(throwable ->
                        throwable instanceof DataIntegrityViolationException &&
                                throwable.getMessage().equals("Menu with given key already exists")
                )
                .verify();
    }

    @Test
    void save_ShouldPropagateError_WhenUserNotFound() {
        when(userServiceClient.getById(1L))
                .thenReturn(Mono.error(new ItemNotFoundException("User not found")));

        StepVerifier.create(menuService.save(testMenu))
                .expectError(ItemNotFoundException.class)
                .verify();

        verify(menuRepository, never()).save(any(Menu.class));
    }

    @Test
    void update_ShouldUpdateMenu_WhenMenuExistsAndNoKeyConflict() {
        Menu updatedMenu = new Menu();
        updatedMenu.setId(1L);
        updatedMenu.setMeal(Meal.LUNCH);
        updatedMenu.setDate(LocalDate.of(2024, 1, 16));
        updatedMenu.setUserId(1L);

        when(menuRepository.findById(1L))
                .thenReturn(Mono.just(testMenu));
        when(menuRepository.findByMealAndDateAndUserId(
                eq(Meal.LUNCH), eq(LocalDate.of(2024, 1, 16)), eq(1L)
        )).thenReturn(Mono.empty());
        when(menuRepository.save(any(Menu.class)))
                .thenReturn(Mono.just(updatedMenu));

        StepVerifier.create(menuService.update(updatedMenu))
                .verifyComplete();

        verify(menuRepository).save(argThat(menu ->
                menu.getMeal() == Meal.LUNCH &&
                        menu.getDate().equals(LocalDate.of(2024, 1, 16))
        ));
    }

    @Test
    void update_ShouldThrowItemNotFoundException_WhenMenuNotFound() {
        when(menuRepository.findById(999L))
                .thenReturn(Mono.empty());

        Menu nonExistentMenu = new Menu();
        nonExistentMenu.setId(999L);

        StepVerifier.create(menuService.update(nonExistentMenu))
                .expectErrorMatches(throwable ->
                        throwable instanceof ItemNotFoundException &&
                                throwable.getMessage().equals("Menu with id 999 was not found")
                )
                .verify();
    }

    @Test
    void update_ShouldThrowDataIntegrityViolationException_WhenNewKeyAlreadyExists() {
        Menu existingMenuWithSameKey = new Menu();
        existingMenuWithSameKey.setId(2L);
        existingMenuWithSameKey.setMeal(Meal.LUNCH);
        existingMenuWithSameKey.setDate(LocalDate.of(2024, 1, 16));
        existingMenuWithSameKey.setUserId(1L);

        Menu updatedMenu = new Menu();
        updatedMenu.setId(1L);
        updatedMenu.setMeal(Meal.LUNCH);
        updatedMenu.setDate(LocalDate.of(2024, 1, 16));
        updatedMenu.setUserId(1L);

        when(menuRepository.findById(1L))
                .thenReturn(Mono.just(testMenu));
        when(menuRepository.findByMealAndDateAndUserId(
                eq(Meal.LUNCH), eq(LocalDate.of(2024, 1, 16)), eq(1L)
        )).thenReturn(Mono.just(existingMenuWithSameKey));

        StepVerifier.create(menuService.update(updatedMenu))
                .expectErrorMatches(throwable ->
                        throwable instanceof DataIntegrityViolationException &&
                                throwable.getMessage().equals("Menu with given new key already exists")
                )
                .verify();
    }

    @Test
    void delete_ShouldDeleteMenu_WhenMenuExists() {
        when(menuRepository.findById(1L))
                .thenReturn(Mono.just(testMenu));
        when(menuRepository.deleteById(1L))
                .thenReturn(Mono.empty());

        StepVerifier.create(menuService.delete(1L))
                .verifyComplete();

        verify(menuRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowItemNotFoundException_WhenMenuNotFound() {
        when(menuRepository.findById(999L))
                .thenReturn(Mono.empty());

        StepVerifier.create(menuService.delete(999L))
                .expectErrorMatches(throwable ->
                        throwable instanceof ItemNotFoundException &&
                                throwable.getMessage().equals("Menu with id 999 was not found")
                )
                .verify();

        verify(menuRepository, never()).deleteById(anyLong());
    }

    @Test
    void findById_ShouldReturnMenu_WhenMenuExists() {
        when(menuRepository.findById(1L))
                .thenReturn(Mono.just(testMenu));

        StepVerifier.create(menuService.findById(1L))
                .expectNext(testMenu)
                .verifyComplete();
    }

    @Test
    void findById_ShouldReturnEmpty_WhenMenuDoesNotExist() {
        when(menuRepository.findById(999L))
                .thenReturn(Mono.empty());

        StepVerifier.create(menuService.findById(999L))
                .verifyComplete();
    }

    @Test
    void includeDishToMenu_ShouldAddDish_WhenBothExistAndDishNotInMenu() {
        when(menuRepository.findById(1L))
                .thenReturn(Mono.just(testMenu));
        when(menuDishesService.getDishesIdByMenuId(1L))
                .thenReturn(Flux.empty());
        when(dishServiceClient.getById(100L))
                .thenReturn(Mono.just(testDishDto));
        when(menuDishesService.saveByIds(1L, 100L))
                .thenReturn(Mono.empty());

        StepVerifier.create(menuService.includeDishToMenu(100L, 1L))
                .verifyComplete();

        verify(menuDishesService).saveByIds(1L, 100L);
    }

    @Test
    void includeDishToMenu_ShouldThrowItemNotFoundException_WhenMenuNotFound() {
        when(menuRepository.findById(999L))
                .thenReturn(Mono.empty());

        StepVerifier.create(menuService.includeDishToMenu(100L, 999L))
                .expectErrorMatches(throwable ->
                        throwable instanceof ItemNotFoundException &&
                                throwable.getMessage().equals("Menu with id 999 was not found")
                )
                .verify();
    }

    @Test
    void includeDishToMenu_ShouldThrowDataIntegrityViolationException_WhenDishAlreadyInMenu() {
        when(menuRepository.findById(1L))
                .thenReturn(Mono.just(testMenu));
        when(menuDishesService.getDishesIdByMenuId(1L))
                .thenReturn(Flux.just(100L));

        StepVerifier.create(menuService.includeDishToMenu(100L, 1L))
                .expectErrorMatches(throwable ->
                        throwable instanceof DataIntegrityViolationException &&
                                throwable.getMessage().equals("Dish with id 100 already in menu with id 1")
                )
                .verify();

        verify(dishServiceClient, never()).getById(anyLong());
        verify(menuDishesService, never()).saveByIds(anyLong(), anyLong());
    }

    @Test
    void deleteDishFromMenu_ShouldRemoveDish_WhenBothExistAndDishInMenu() {
        when(menuRepository.findById(1L))
                .thenReturn(Mono.just(testMenu));
        when(menuDishesService.getDishesIdByMenuId(1L))
                .thenReturn(Flux.just(100L));
        when(menuDishesService.deleteByIds(1L, 100L))
                .thenReturn(Mono.empty());

        StepVerifier.create(menuService.deleteDishFromMenu(100L, 1L))
                .verifyComplete();

        verify(menuDishesService).deleteByIds(1L, 100L);
    }

    @Test
    void deleteDishFromMenu_ShouldThrowItemNotFoundException_WhenDishNotInMenu() {
        when(menuRepository.findById(1L))
                .thenReturn(Mono.just(testMenu));
        when(menuDishesService.getDishesIdByMenuId(1L))
                .thenReturn(Flux.empty());
        when(menuDishesService.deleteByIds(anyLong(), anyLong()))
                .thenReturn(Mono.empty());

        StepVerifier.create(menuService.deleteDishFromMenu(100L, 1L))
                .expectErrorMatches(throwable ->
                        throwable instanceof ItemNotFoundException &&
                                throwable.getMessage().equals("Dish with id 100 is not in menu with id 1")
                )
                .verify();

        verify(menuDishesService, never()).deleteByIds(anyLong(), anyLong());
    }

    @Test
    void makeListOfDishes_ShouldReturnDishes_WhenMenuExists() {
        when(menuRepository.findById(1L))
                .thenReturn(Mono.just(testMenu));
        when(menuDishesService.getDishesIdByMenuId(1L))
                .thenReturn(Flux.just(100L));
        when(dishServiceClient.getById(100L))
                .thenReturn(Mono.just(testDishDto));

        StepVerifier.create(menuService.makeListOfDishes(1L))
                .expectNext(testDishDto)
                .verifyComplete();
    }

    @Test
    void makeListOfDishes_ShouldReturnNotFoundDish_WhenDishServiceFails() {
        when(menuRepository.findById(1L))
                .thenReturn(Mono.just(testMenu));
        when(menuDishesService.getDishesIdByMenuId(1L))
                .thenReturn(Flux.just(999L));
        when(dishServiceClient.getById(999L))
                .thenReturn(Mono.error(new ItemNotFoundException("Dish not found")));

        StepVerifier.create(menuService.makeListOfDishes(1L))
                .expectNextMatches(dish ->
                        dish.id().equals(999L) &&
                                dish.name().equals("(not found)")
                )
                .verifyComplete();
    }

    @Test
    void findAll_ShouldReturnPaginatedMenus() {
        Menu menu1 = new Menu();
        menu1.setId(1L);
        Menu menu2 = new Menu();
        menu2.setId(2L);

        when(menuRepository.findAll())
                .thenReturn(Flux.just(menu1, menu2));

        StepVerifier.create(menuService.findAll(0, 10))
                .expectNext(menu1, menu2)
                .verifyComplete();
    }

    @Test
    void findAllByUsername_ShouldReturnMenusForUser() {
        when(userServiceClient.getByName("TestUser"))
                .thenReturn(Mono.just(testUserDto));
        when(menuRepository.findAllByUserId(1L))
                .thenReturn(Flux.just(testMenu));

        StepVerifier.create(menuService.findAllByUsername("TestUser"))
                .expectNext(testMenu)
                .verifyComplete();
    }

    @Test
    void findAllByUsername_ShouldPropagateError_WhenUserNotFound() {
        when(userServiceClient.getByName("NonExistent"))
                .thenReturn(Mono.error(new ItemNotFoundException("User not found")));

        StepVerifier.create(menuService.findAllByUsername("NonExistent"))
                .expectError(ItemNotFoundException.class)
                .verify();
    }
}