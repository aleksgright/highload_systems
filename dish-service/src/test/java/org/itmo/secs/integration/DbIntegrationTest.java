package org.itmo.secs.integration;

import org.itmo.secs.model.entities.Item;
import org.itmo.secs.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest
public class DbIntegrationTest {
    @Container
    static PostgreSQLContainer<?> pgContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test-db")
            .withUsername("postgres")
            .withPassword("password");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", pgContainer::getJdbcUrl);
        registry.add("spring.datasource.username", pgContainer::getUsername);
        registry.add("spring.datasource.password", pgContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private DishRepository dishRepository;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        dishRepository.deleteAll();

        List<Item> items = List.of(
                new Item(1L, 300, "Milk1", 20, 10, 50, new ArrayList<>()),
                new Item(2L, 300, "Milk2", 20, 10, 50, new ArrayList<>()),
                new Item(3L, 300, "Milk3", 20, 10, 50, new ArrayList<>()),
                new Item(4L, 300, "Milk4", 20, 10, 50, new ArrayList<>()),
                new Item(5L, 300, "Milk5", 20, 10, 50, new ArrayList<>()));

        itemRepository.saveAll(items);
    }

    @Test
    void testSaveAndFindItem() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Boris");
        item.setCalories(300);
        item.setFats(299);
        item.setCarbs(298);
        item.setProtein(287);
        itemRepository.save(item);

        Item savedItem = itemRepository.save(item);

        assertNotNull(savedItem.getId());

        Optional<Item> optItem = itemRepository.findByName("Boris");
        assertTrue(optItem.isPresent());
    }
}
