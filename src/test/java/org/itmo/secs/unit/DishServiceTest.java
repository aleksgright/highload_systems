package org.itmo.secs.unit;

import org.itmo.secs.model.entities.Dish;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.model.entities.ItemDish;
import org.itmo.secs.model.entities.ItemDishId;
import org.itmo.secs.repositories.DishRepository;
import org.itmo.secs.services.DishService;
import org.itmo.secs.services.ItemDishService;
import org.itmo.secs.services.ItemService;
import org.itmo.secs.utils.exceptions.DataIntegrityViolationException;
import org.itmo.secs.utils.exceptions.ItemNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DishServiceTest {
    private final ItemDishService itemDishService = Mockito.mock(ItemDishService.class);
    private final DishRepository dishRepository= Mockito.mock(DishRepository.class);
    private final ItemService itemService= Mockito.mock(ItemService.class);

    @InjectMocks
    private DishService dishService = new DishService(itemDishService, dishRepository, itemService);

    private Dish testDish;
    private Dish existingDish;
    private Item testItem;
    private ItemDish testItemDish;

    @BeforeEach
    void setUp() {
        testDish = new Dish();
        testDish.setId(1L);
        testDish.setName("Test Dish");

        existingDish = new Dish();
        existingDish.setId(2L);
        existingDish.setName("Existing Dish");

        testItem = new Item();
        testItem.setId(100L);
        testItem.setName("Test Item");

        testItemDish = new ItemDish();
        testItemDish.setId(new ItemDishId(100L, 1L));
        testItemDish.setItem(testItem);
        testItemDish.setDish(testDish);
        testItemDish.setCount(3);
    }

    @Test
    void save_ShouldSaveDish_WhenDishDoesNotExist() {
        when(dishRepository.findByName(testDish.getName())).thenReturn(Optional.empty());
        when(dishRepository.save(testDish)).thenReturn(testDish);

        Dish savedDish = dishService.save(testDish);

        assertNotNull(savedDish);
        assertEquals(testDish.getId(), savedDish.getId());
        assertEquals(testDish.getName(), savedDish.getName());

        verify(dishRepository).findByName(testDish.getName());
        verify(dishRepository).save(testDish);
    }

    @Test
    void save_ShouldThrowDataIntegrityViolationException_WhenDishWithSameNameExists() {
        when(dishRepository.findByName(testDish.getName())).thenReturn(Optional.of(existingDish));

        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> dishService.save(testDish)
        );

        assertEquals("Dish with name Test Dish already exist", exception.getMessage());

        verify(dishRepository).findByName(testDish.getName());
        verify(dishRepository, never()).save(any(Dish.class));
    }

    @Test
    void save_ShouldHandleNullDish() {
        assertThrows(NullPointerException.class, () -> dishService.save(null));
    }

    @Test
    void addItem_ShouldAddItemToDish_WhenBothExist() {
        when(dishRepository.findById(1L)).thenReturn(Optional.of(testDish));
        when(itemService.findById(100L)).thenReturn(testItem);

        dishService.addItem(100L, 1L, 5);

        verify(dishRepository).findById(1L);
        verify(itemService).findById(100L);
        verify(itemDishService).updateItemDishCount(testItem, testDish, 5);
    }

    @Test
    void addItem_ShouldHandleNegativeCount() {
        when(dishRepository.findById(1L)).thenReturn(Optional.of(testDish));
        when(itemService.findById(100L)).thenReturn(testItem);

        dishService.addItem(100L, 1L, -2);

        verify(itemDishService).updateItemDishCount(testItem, testDish, -2);
    }

    @Test
    void addItem_ShouldHandleZeroCount() {
        when(dishRepository.findById(1L)).thenReturn(Optional.of(testDish));
        when(itemService.findById(100L)).thenReturn(testItem);

        dishService.addItem(100L, 1L, 0);

        verify(itemDishService).updateItemDishCount(testItem, testDish, 0);
    }

    @Test
    void findById_ShouldReturnDish_WhenDishExists() {
        when(dishRepository.findById(1L)).thenReturn(Optional.of(testDish));

        Dish foundDish = dishService.findById(1L);

        assertNotNull(foundDish);
        assertEquals(testDish.getId(), foundDish.getId());
        assertEquals(testDish.getName(), foundDish.getName());

        verify(dishRepository).findById(1L);
    }

    @Test
    void findById_ShouldHandleNullId() {
        Dish foundDish = dishService.findById(null);

        assertNull(foundDish);
        verify(dishRepository).findById(null);
    }

    @Test
    void findByName_ShouldReturnDish_WhenDishExists() {
        when(dishRepository.findByName("Test Dish")).thenReturn(Optional.of(testDish));

        Dish foundDish = dishService.findByName("Test Dish");

        assertNotNull(foundDish);
        assertEquals(testDish.getName(), foundDish.getName());

        verify(dishRepository).findByName("Test Dish");
    }

    @Test
    void makeListOfItems_ShouldReturnEmptyList_WhenNoItems() {
        when(itemDishService.findAllByDishId(1L)).thenReturn(new ArrayList<>());

        List<Pair<Item, Integer>> result = dishService.makeListOfItems(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(itemDishService).findAllByDishId(1L);
    }

    @Test
    void makeListOfItems_ShouldHandleNonExistentDishId() {
        when(itemDishService.findAllByDishId(999L)).thenReturn(new ArrayList<>());

        List<Pair<Item, Integer>> result = dishService.makeListOfItems(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_ShouldReturnPaginatedDishes() {
        List<Dish> dishes = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Dish dish = new Dish();
            dish.setId((long) i);
            dish.setName("Dish " + i);
            dishes.add(dish);
        }

        Page<Dish> dishPage = new PageImpl<>(
                dishes.subList(2, 5),
                PageRequest.of(1, 3),
                dishes.size()
        );

        when(dishRepository.findAll(any(Pageable.class))).thenReturn(dishPage);

        List<Dish> result = dishService.findAll(1, 3);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Dish 3", result.get(0).getName());
        assertEquals("Dish 4", result.get(1).getName());
        assertEquals("Dish 5", result.get(2).getName());

        verify(dishRepository).findAll(PageRequest.of(1, 3));
    }

    @Test
    void findAll_ShouldHandleFirstPage() {
        List<Dish> dishes = new ArrayList<>();
        dishes.add(testDish);

        Page<Dish> dishPage = new PageImpl<>(dishes, PageRequest.of(0, 10), 1);
        when(dishRepository.findAll(PageRequest.of(0, 10))).thenReturn(dishPage);

        List<Dish> result = dishService.findAll(0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDish, result.get(0));
    }

    @Test
    void findAll_ShouldHandleLargePageSize() {
        List<Dish> dishes = new ArrayList<>();
        dishes.add(testDish);

        Page<Dish> dishPage = new PageImpl<>(dishes, PageRequest.of(0, 100), 1);
        when(dishRepository.findAll(PageRequest.of(0, 100))).thenReturn(dishPage);

        List<Dish> result = dishService.findAll(0, 100);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void save_ShouldHandleNullName() {
        Dish dishWithNullName = new Dish();
        dishWithNullName.setId(1L);
        dishWithNullName.setName(null);

        when(dishRepository.findByName(null)).thenReturn(Optional.empty());
        when(dishRepository.save(dishWithNullName)).thenReturn(dishWithNullName);

        assertDoesNotThrow(() -> dishService.save(dishWithNullName));

        verify(dishRepository).findByName(null);
        verify(dishRepository).save(dishWithNullName);
    }

    @Test
    void save_ShouldHandleEmptyName() {
        Dish dishWithEmptyName = new Dish();
        dishWithEmptyName.setId(1L);
        dishWithEmptyName.setName("");

        when(dishRepository.findByName("")).thenReturn(Optional.empty());
        when(dishRepository.save(dishWithEmptyName)).thenReturn(dishWithEmptyName);

        assertDoesNotThrow(() -> dishService.save(dishWithEmptyName));

        verify(dishRepository).findByName("");
        verify(dishRepository).save(dishWithEmptyName);
    }

    @Test
    void updateName_ShouldUpdateOnlyName_WhenDishExists() {
        Dish originalDish = new Dish();
        originalDish.setId(1L);
        originalDish.setName("Original Name");

        Dish updateRequest = new Dish();
        updateRequest.setId(1L);
        updateRequest.setName("New Name");

        when(dishRepository.findById(1L)).thenReturn(Optional.of(originalDish));
        when(dishRepository.save(updateRequest)).thenReturn(updateRequest);

        dishService.updateName(updateRequest);

        verify(dishRepository).save(argThat(dish ->
                dish.getId().equals(1L) &&
                        dish.getName().equals("New Name")
        ));
    }

    @Test
    void addItem_ShouldNotAddItemToCollections() {
        when(dishRepository.findById(1L)).thenReturn(Optional.of(testDish));
        when(itemService.findById(100L)).thenReturn(testItem);

        dishService.addItem(100L, 1L, 3);

        verify(itemDishService).updateItemDishCount(testItem, testDish, 3);
    }

    @Test
    void deleteItem_ShouldNotRemoveItemFromCollections() {
        when(dishRepository.findById(1L)).thenReturn(Optional.of(testDish));
        when(itemService.findById(100L)).thenReturn(testItem);

        dishService.deleteItem(100L, 1L);

        verify(itemDishService).delete(testItem, testDish);
    }
}