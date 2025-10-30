package org.itmo.secs.integration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.itmo.secs.model.entities.Item;
import org.itmo.secs.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
public class DishControllerTest {
    @LocalServerPort
    private Integer port;

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
                new Item(1L, 300, "Milk1", 20, 10, 50, 0L),
                new Item(2L, 300, "Milk2", 20, 10, 50, 0L),
                new Item(3L, 300, "Milk3", 20, 10, 50, 0L),
                new Item(4L, 300, "Milk4", 20, 10, 50, 0L),
                new Item(5L, 300, "Milk5", 20, 10, 50, 0L));

        List<Item> savedItems = itemRepository.saveAll(items);
    }
}
