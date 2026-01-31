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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class MenuServiceTest {
    private final MenuRepository menuRepository = Mockito.mock(MenuRepository.class);
    private final MenuDishesService menuDishesService = Mockito.mock(MenuDishesService.class);
    private final DishServiceClient dishService = Mockito.mock(DishServiceClient.class);
    private final UserServiceClient userService = Mockito.mock(UserServiceClient.class);

    private final MenuService menuService = new MenuService(menuRepository, menuDishesService, dishService, userService);

    private Menu testMenu;
    private Menu existingMenu;
    private UserDto testUser;
    private DishDto testDish;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2024, 1, 15);

        testUser = new UserDto(1L, "Test User");

        testDish = new DishDto(100L, "Test Dish", 1, 1, 1, 1);

        testMenu = new Menu();
        testMenu.setId(1L);
        testMenu.setMeal(Meal.BREAKFAST);
        testMenu.setDate(testDate);
        existingMenu.setUserId(null);

        existingMenu = new Menu();
        existingMenu.setId(2L);
        existingMenu.setMeal(Meal.LUNCH);
        existingMenu.setDate(testDate.plusDays(1));
        existingMenu.setUserId(null);
    }

    @Test
    void save_ShouldSaveMenu_WhenMenuDoesNotExist() {
        when(menuRepository.findByMealAndDateAndUserId(
                testMenu.getMeal(),
                testMenu.getDate(),
                testUser.id()
        )).thenReturn(Mono.empty());

        when(menuRepository.save(testMenu)).thenReturn(Mono.just(testMenu));

        Mono<Menu> savedMenu = menuService.save(testMenu);

        assertNotNull(savedMenu);
        assertEquals(testMenu.getId(), savedMenu.getId());
        assertEquals(testMenu.getMeal(), savedMenu.getMeal());

        verify(menuRepository).findByMealAndDateAndUserId(
                testMenu.getMeal(),
                testMenu.getDate(),
                testUser.getId()
        );
        verify(menuRepository).save(testMenu);
    }

    @Test
    void save_ShouldSaveGlobalMenu_WhenUserIsNull() {
        Menu globalMenu = new Menu();
        globalMenu.setId(3L);
        globalMenu.setMeal(Meal.DINNER);
        globalMenu.setDate(testDate);
        globalMenu.setUser(null);

        when(menuRepository.findByMealAndDateAndUserId(
                Meal.DINNER,
                testDate,
                null
        )).thenReturn(Optional.empty());

        when(menuRepository.save(globalMenu)).thenReturn(globalMenu);

        Menu savedMenu = menuService.save(globalMenu);

        assertNotNull(savedMenu);
        assertNull(savedMenu.getUser());

        verify(menuRepository).findByMealAndDateAndUserId(
                Meal.DINNER,
                testDate,
                null
        );
    }

    @Test
    void save_ShouldThrowDataIntegrityViolationException_WhenMenuWithSameKeyExists() {
        when(menuRepository.findByMealAndDateAndUserId(
                testMenu.getMeal(),
                testMenu.getDate(),
                testUser.getId()
        )).thenReturn(Optional.of(existingMenu));

        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> menuService.save(testMenu)
        );

        assertEquals("Menu with given key already exists", exception.getMessage());

        verify(menuRepository).findByMealAndDateAndUserId(
                testMenu.getMeal(),
                testMenu.getDate(),
                testUser.getId()
        );
        verify(menuRepository, never()).save(any(Menu.class));
    }

    @Test
    void update_ShouldThrowItemNotFoundException_WhenMenuDoesNotExist() {
        Menu nonExistentMenu = new Menu();
        nonExistentMenu.setId(999L);

        when(menuRepository.findById(999L)).thenReturn(Optional.empty());

        ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> menuService.update(nonExistentMenu)
        );

        assertEquals("Menu with id 999 was not found", exception.getMessage());

        verify(menuRepository).findById(999L);
        verify(menuRepository, never()).findByMealAndDateAndUserId(any(), any(), any());
        verify(menuRepository, never()).save(any(Menu.class));
    }

    @Test
    void update_ShouldUpdateGlobalMenu() {
        Menu globalMenu = new Menu();
        globalMenu.setId(1L);
        globalMenu.setMeal(Meal.BREAKFAST);
        globalMenu.setDate(testDate);
        globalMenu.setUser(null);

        Menu updatedGlobalMenu = new Menu();
        updatedGlobalMenu.setId(1L);
        updatedGlobalMenu.setMeal(Meal.LUNCH);
        updatedGlobalMenu.setDate(testDate);
        updatedGlobalMenu.setUser(null);

        when(menuRepository.findById(1L)).thenReturn(Optional.of(globalMenu));
        when(menuRepository.findByMealAndDateAndUserId(
                Meal.LUNCH,
                testDate,
                null
        )).thenReturn(Optional.empty());
        when(menuRepository.save(updatedGlobalMenu)).thenReturn(updatedGlobalMenu);

        menuService.update(updatedGlobalMenu);

        verify(menuRepository).save(updatedGlobalMenu);
    }

    @Test
    void delete_ShouldDeleteMenu_WhenMenuExists() {
        when(menuRepository.findById(1L)).thenReturn(Optional.of(testMenu));
        doNothing().when(menuRepository).deleteById(1L);

        menuService.delete(1L);

        verify(menuRepository).findById(1L);
        verify(menuRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowItemNotFoundException_WhenMenuDoesNotExist() {
        when(menuRepository.findById(999L)).thenReturn(Optional.empty());

        ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> menuService.delete(999L)
        );

        assertEquals("Menu with id 999 was not found", exception.getMessage());

        verify(menuRepository).findById(999L);
        verify(menuRepository, never()).deleteById(anyLong());
    }

    @Test
    void findById_ShouldReturnMenu_WhenMenuExists() {
        when(menuRepository.findById(1L)).thenReturn(Optional.of(testMenu));

        Menu foundMenu = menuService.findById(1L);

        assertNotNull(foundMenu);
        assertEquals(testMenu.getId(), foundMenu.getId());

        verify(menuRepository).findById(1L);
    }

    @Test
    void findById_ShouldReturnNull_WhenMenuDoesNotExist() {
        when(menuRepository.findById(999L)).thenReturn(Optional.empty());

        Menu foundMenu = menuService.findById(999L);

        assertNull(foundMenu);

        verify(menuRepository).findById(999L);
    }

    @Test
    void findByKey_ShouldReturnMenu_WhenMenuExists() {
        when(menuRepository.findByMealAndDateAndUserId(
                Meal.BREAKFAST,
                testDate,
                1L
        )).thenReturn(Optional.of(testMenu));

        Menu foundMenu = menuService.findByKey(Meal.BREAKFAST, testDate, 1L);

        assertNotNull(foundMenu);
        assertEquals(testMenu.getId(), foundMenu.getId());

        verify(menuRepository).findByMealAndDateAndUserId(
                Meal.BREAKFAST,
                testDate,
                1L
        );
    }

    @Test
    void findByKey_ShouldReturnGlobalMenu_WhenUserIdIsNull() {
        Menu globalMenu = new Menu();
        globalMenu.setId(3L);
        globalMenu.setUser(null);

        when(menuRepository.findByMealAndDateAndUserId(
                Meal.DINNER,
                testDate,
                null
        )).thenReturn(Optional.of(globalMenu));

        Menu foundMenu = menuService.findByKey(Meal.DINNER, testDate, null);

        assertNotNull(foundMenu);
        assertNull(foundMenu.getUser());
    }

    @Test
    void findByKey_ShouldReturnNull_WhenMenuDoesNotExist() {
        when(menuRepository.findByMealAndDateAndUserId(
                Meal.DINNER,
                testDate,
                999L
        )).thenReturn(Optional.empty());

        Menu foundMenu = menuService.findByKey(Meal.DINNER, testDate, 999L);

        assertNull(foundMenu);
    }

    @Test
    void includeDishToMenu_ShouldAddDish_WhenBothExist() {
        when(menuRepository.findById(1L)).thenReturn(Optional.of(testMenu));
        when(dishService.findById(100L)).thenReturn(testDish);
        when(menuRepository.save(testMenu)).thenReturn(testMenu);

        menuService.includeDishToMenu(100L, 1L);

        assertTrue(testMenu.getDishes().contains(testDish));
        verify(menuRepository).findById(1L);
        verify(dishService).findById(100L);
        verify(menuRepository).save(testMenu);
    }

    @Test
    void includeDishToMenu_ShouldThrowItemNotFoundException_WhenMenuNotFound() {
        when(menuRepository.findById(999L)).thenReturn(Optional.empty());

        ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> menuService.includeDishToMenu(100L, 999L)
        );

        assertEquals("Menu with id 999 was not found", exception.getMessage());

        verify(menuRepository).findById(999L);
        verify(dishService, never()).findById(anyLong());
    }

    @Test
    void includeDishToMenu_ShouldThrowItemNotFoundException_WhenDishNotFound() {
        when(menuRepository.findById(1L)).thenReturn(Optional.of(testMenu));
        when(dishService.findById(999L)).thenReturn(null);

        ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> menuService.includeDishToMenu(999L, 1L)
        );

        assertEquals("Dish with id 999 was not found", exception.getMessage());

        verify(menuRepository).findById(1L);
        verify(dishService).findById(999L);
        verify(menuRepository, never()).save(any(Menu.class));
    }

    @Test
    void deleteDishFromMenu_ShouldRemoveDish_WhenBothExist() {
        testMenu.getDishes().add(testDish);

        when(menuRepository.findById(1L)).thenReturn(Optional.of(testMenu));
        when(dishService.findById(100L)).thenReturn(testDish);
        when(menuRepository.save(testMenu)).thenReturn(testMenu);

        menuService.deleteDishFromMenu(100L, 1L);

        assertFalse(testMenu.getDishes().contains(testDish));
        verify(menuRepository).findById(1L);
        verify(dishService).findById(100L);
        verify(menuRepository).save(testMenu);
    }

    @Test
    void deleteDishFromMenu_ShouldHandleNonExistentDishInMenu() {
        when(menuRepository.findById(1L)).thenReturn(Optional.of(testMenu));
        when(dishService.findById(100L)).thenReturn(testDish);
        when(menuRepository.save(testMenu)).thenReturn(testMenu);

        menuService.deleteDishFromMenu(100L, 1L);

        assertTrue(testMenu.getDishes().isEmpty());
        verify(menuRepository).save(testMenu);
    }

    @Test
    void deleteDishFromMenu_ShouldThrowItemNotFoundException_WhenMenuNotFound() {
        when(menuRepository.findById(999L)).thenReturn(Optional.empty());

        ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> menuService.deleteDishFromMenu(100L, 999L)
        );

        assertEquals("Menu with id 999 was not found", exception.getMessage());
    }

    @Test
    void makeListOfDishes_ShouldReturnDishes_WhenMenuExists() {
        Dish dish2 = new Dish();
        dish2.setId(200L);
        dish2.setName("Another Dish");

        testMenu.getDishes().add(testDish);
        testMenu.getDishes().add(dish2);

        when(menuRepository.findById(1L)).thenReturn(Optional.of(testMenu));

        List<Dish> dishes = menuService.makeListOfDishes(1L);

        assertNotNull(dishes);
        assertEquals(2, dishes.size());
        assertTrue(dishes.contains(testDish));
        assertTrue(dishes.contains(dish2));

        verify(menuRepository).findById(1L);
    }

    @Test
    void makeListOfDishes_ShouldReturnEmptyList_WhenMenuHasNoDishes() {
        when(menuRepository.findById(1L)).thenReturn(Optional.of(testMenu));

        List<Dish> dishes = menuService.makeListOfDishes(1L);

        assertNotNull(dishes);
        assertTrue(dishes.isEmpty());
    }

    @Test
    void makeListOfDishes_ShouldThrowItemNotFoundException_WhenMenuNotFound() {
        when(menuRepository.findById(999L)).thenReturn(Optional.empty());

        ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> menuService.makeListOfDishes(999L)
        );

        assertEquals("Menu with id 999 was not found", exception.getMessage());
    }

    @Test
    void findAll_ShouldReturnPaginatedMenus() {
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        List<Menu> menus = List.of(testMenu, existingMenu);
        Page<Menu> pageResult = new PageImpl<>(menus, pageable, menus.size());

        when(menuRepository.findAll(pageable)).thenReturn(pageResult);

        List<Menu> result = menuService.findAll(page, size);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(menuRepository).findAll(pageable);
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoMenus() {
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        Page<Menu> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(menuRepository.findAll(pageable)).thenReturn(emptyPage);

        List<Menu> result = menuService.findAll(page, size);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void update_ShouldHandleNullUserInExistingMenu() {
        Menu menuWithNullUser = new Menu();
        menuWithNullUser.setId(1L);
        menuWithNullUser.setUser(null);

        Menu updatedMenu = new Menu();
        updatedMenu.setId(1L);
        updatedMenu.setMeal(Meal.LUNCH);
        updatedMenu.setDate(testDate);
        updatedMenu.setUser(testUser);

        when(menuRepository.findById(1L)).thenReturn(Optional.of(menuWithNullUser));
        when(menuRepository.findByMealAndDateAndUserId(
                Meal.LUNCH,
                testDate,
                testUser.getId()
        )).thenReturn(Optional.empty());
        when(menuRepository.save(updatedMenu)).thenReturn(updatedMenu);

        menuService.update(updatedMenu);

        verify(menuRepository).save(updatedMenu);
    }

    @Test
    void update_ShouldHandleSettingUserToNull() {
        Menu menuWithUser = new Menu();
        menuWithUser.setId(1L);
        menuWithUser.setUser(testUser);

        Menu updatedMenu = new Menu();
        updatedMenu.setId(1L);
        updatedMenu.setMeal(Meal.LUNCH);
        updatedMenu.setDate(testDate);
        updatedMenu.setUser(null);

        when(menuRepository.findById(1L)).thenReturn(Optional.of(menuWithUser));
        when(menuRepository.findByMealAndDateAndUserId(
                Meal.LUNCH,
                testDate,
                null
        )).thenReturn(Optional.empty());
        when(menuRepository.save(updatedMenu)).thenReturn(updatedMenu);

        menuService.update(updatedMenu);

        verify(menuRepository).save(updatedMenu);
    }

    @Test
    void findByKey_ShouldHandleAllNullParameters() {
        Menu result = menuService.findByKey(null, null, null);

        assertNull(result);
        verify(menuRepository).findByMealAndDateAndUserId(null, null, null);
    }
}