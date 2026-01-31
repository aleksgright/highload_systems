package org.itmo.secs.unit;

import org.itmo.secs.model.entities.Item;
import org.itmo.secs.repositories.ItemRepository;
import org.itmo.secs.services.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ItemServiceTest {
    private final ItemRepository itemRepository = Mockito.mock(ItemRepository.class);
    private final ItemService itemService = new ItemService(itemRepository);

    private Item testItem;

    @BeforeEach
    void setUp() {
        testItem = createItem(1L, "Test Item", 300, 20, 10, 50);
    }

    @Test
    void findById_ShouldReturnNull_WhenItemDoesNotExist() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        Item foundItem = itemService.findById(999L);

        assertNull(foundItem);

        verify(itemRepository).findById(999L);
    }


    @Test
    void findByName_ShouldReturnItem_WhenItemExists() {
        when(itemRepository.findByName("Test Item")).thenReturn(Optional.of(testItem));

        Item foundItem = itemService.findByName("Test Item");

        assertNotNull(foundItem);
        assertEquals(testItem.getName(), foundItem.getName());

        verify(itemRepository).findByName("Test Item");
    }

    @Test
    void findByName_ShouldReturnNull_WhenItemDoesNotExist() {
        when(itemRepository.findByName("Non Existent Item")).thenReturn(Optional.empty());

        Item foundItem = itemService.findByName("Non Existent Item");

        assertNull(foundItem);

        verify(itemRepository).findByName("Non Existent Item");
    }

    @Test
    void findByName_ShouldHandleNullName() {
        Item foundItem = itemService.findByName(null);

        assertNull(foundItem);

        verify(itemRepository).findByName(null);
    }

    @Test
    void findAll_ShouldReturnPaginatedItems() {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<Item> items = List.of(
                createItem(1L, "Milk1", 300, 20, 10, 50),
                createItem(2L, "Milk2", 300, 20, 10, 50),
                createItem(3L, "Milk3", 300, 20, 10, 50),
                createItem(4L, "Milk4", 300, 20, 10, 50),
                createItem(5L, "Milk5", 300, 20, 10, 50));

        Page<Item> page = new PageImpl<>(items, pageable, items.size());

        when(itemRepository.findAll(pageable)).thenReturn(page);

        List<Item> result = itemService.findAll(pageNumber, pageSize);

        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("Milk1", result.get(0).getName());
        assertEquals("Milk5", result.get(4).getName());

        verify(itemRepository).findAll(pageable);
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoItems() {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Item> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(itemRepository.findAll(pageable)).thenReturn(emptyPage);

        List<Item> result = itemService.findAll(pageNumber, pageSize);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(itemRepository).findAll(pageable);
    }

    @Test
    void findAll_ShouldHandleSecondPage() {
        int pageNumber = 1;
        int pageSize = 2;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<Item> items = List.of(
                createItem(3L, "Milk3", 300, 20, 10, 50),
                createItem(4L, "Milk4", 300, 20, 10, 50));

        Page<Item> page = new PageImpl<>(items, pageable, 5);

        when(itemRepository.findAll(pageable)).thenReturn(page);

        List<Item> result = itemService.findAll(pageNumber, pageSize);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Milk3", result.get(0).getName());
        assertEquals("Milk4", result.get(1).getName());

        verify(itemRepository).findAll(pageable);
    }

    @Test
    void count_ShouldReturnItemCount() {
        long expectedCount = 42L;
        when(itemRepository.count()).thenReturn(expectedCount);

        long count = itemService.count();

        assertEquals(expectedCount, count);

        verify(itemRepository).count();
    }

    @Test
    void count_ShouldReturnZero_WhenNoItems() {
        when(itemRepository.count()).thenReturn(0L);

        long count = itemService.count();

        assertEquals(0L, count);

        verify(itemRepository).count();
    }

    private Item createItem(Long id, String name, int calories, int protein, int fat, int carbohydrate) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setCalories(calories);
        item.setProtein(protein);
        item.setFats(fat);
        item.setCarbs(carbohydrate);
        item.setCreatorId(0L);
        return item;
    }
}