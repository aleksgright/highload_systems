package org.itmo.secs.unit;


import org.itmo.secs.model.entities.Item;

import org.itmo.secs.repositories.ItemRepository;
import org.itmo.secs.services.ItemService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

public class ItemServiceTest {
    private final ItemRepository itemRepositoryMock = Mockito.mock(ItemRepository.class);
    @Autowired
    private final ItemService itemService = new ItemService(itemRepositoryMock);

    @Test
    public void findAll() {
        int pageNumber = 1;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<Item> items = List.of(
                new Item(1L, 300, "Milk1", 20, 10, 50, 0L, new ArrayList<>()),
                new Item(2L, 300, "Milk2", 20, 10, 50, 0L, new ArrayList<>()),
                new Item(3L, 300, "Milk3", 20, 10, 50, 0L, new ArrayList<>()),
                new Item(4L, 300, "Milk4", 20, 10, 50, 0L, new ArrayList<>()),
                new Item(5L, 300, "Milk5", 20, 10, 50, 0L, new ArrayList<>()));
        Page<Item> page = new PageImpl<>(items);

        Mockito.when(itemRepositoryMock.findAll(pageable)).thenReturn(page);
        Assertions.assertEquals(items, itemService.findAll(pageNumber, pageSize));
    }

}
