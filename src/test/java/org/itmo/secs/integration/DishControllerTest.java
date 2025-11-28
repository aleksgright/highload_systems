package org.itmo.secs.integration;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import org.itmo.secs.model.dto.DishCreateDto;
import org.itmo.secs.model.dto.DishUpdateNameDto;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.restassured.RestAssured;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DishControllerTest {
    @LocalServerPort
    private String port;
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

        RestAssured.baseURI = "http://localhost:" + port;

        List<Item> items = List.of(
                new Item(1L, 300, "Milk1", 20, 10, 50, 0L, new ArrayList<>()),
                new Item(2L, 300, "Milk2", 20, 10, 50, 0L, new ArrayList<>()),
                new Item(3L, 300, "Milk3", 20, 10, 50, 0L, new ArrayList<>()),
                new Item(4L, 300, "Milk4", 20, 10, 50, 0L, new ArrayList<>()),
                new Item(5L, 300, "Milk5", 20, 10, 50, 0L, new ArrayList<>()));

        List<Item> savedItems = itemRepository.saveAll(items);
    }

    @Test
    void testCreateNewDish() {
        Gson gson = new Gson();

        DishCreateDto requestBodyDto = new DishCreateDto("Someone");

        String requestBody = gson.toJson(requestBodyDto);
        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .post("/dish")
                .then()
                .statusCode(201);

        assertTrue(dishRepository.findById(1L).isPresent());
    }

    @Test
    void testUpdatingNonExistingDish() {
        Gson gson = new Gson();

        DishUpdateNameDto requestBodyDto = new DishUpdateNameDto(100000L, "Someone");

        String requestBody = gson.toJson(requestBodyDto);
        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .put("/dish")
                .then()
                .statusCode(500);
    }
}
