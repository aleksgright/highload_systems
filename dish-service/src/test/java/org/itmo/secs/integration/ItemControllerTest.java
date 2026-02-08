package org.itmo.secs.integration;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import org.itmo.secs.model.dto.ItemCreateDto;
import org.itmo.secs.model.dto.ItemUpdateDto;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.repositories.ItemRepository;
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

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ItemControllerTest {
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
        // Отключаем Liquibase для тестов
        registry.add("spring.liquibase.enabled", () -> "false");
        // Отключаем конфиг сервер для тестов
        registry.add("spring.cloud.config.enabled", () -> "false");
        registry.add("spring.cloud.config.import-check.enabled", () -> "false");
        registry.add("app.max-page-size", () -> "10");
        registry.add("app.default-page-size", () -> "5");
    }

    @Autowired
    private ItemRepository itemRepository;

    private List<Item> items;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();

        RestAssured.baseURI = "http://localhost:" + port;
        RestAssured.port = Integer.parseInt(port);

        // Create items
        items = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Item item = new Item();
            item.setName("TestItem" + i);
            item.setCalories(300 + i * 10);
            item.setProtein(20 + i);
            item.setFats(10 + i);
            item.setCarbs(50 + i * 5);
            items.add(item);
        }
        items = itemRepository.saveAll(items);

        assertFalse(items.isEmpty());
    }

    @Test
    void testCreateNewItem() {
        Gson gson = new Gson();

        ItemCreateDto dto = new ItemCreateDto("NEW_ITEM", 400, 25, 15, 60);

        RestAssured.given()
                .contentType("application/json")
                .body(gson.toJson(dto))
                .post("/item")
                .then()
                .statusCode(201);

        assertTrue(itemRepository.findByName("NEW_ITEM").isPresent());
    }

    @Test
    void testUpdate() {
        Gson gson = new Gson();

        Item item = items.get(0);
        ItemUpdateDto dto = new ItemUpdateDto(
                item.getId(),
                "UPDATED_ITEM",
                500,
                70,
                30,
                20
        );

        RestAssured.given()
                .contentType("application/json")
                .body(gson.toJson(dto))
                .put("/item")
                .then()
                .statusCode(204);

        Item updatedItem = itemRepository.findById(item.getId()).orElseThrow();
        assertEquals("UPDATED_ITEM", updatedItem.getName());
        assertEquals(500, updatedItem.getCalories());
        assertEquals(30, updatedItem.getProtein());
        assertEquals(20, updatedItem.getFats());
        assertEquals(70, updatedItem.getCarbs());
    }

    @Test
    void testFind() {
        Item first = items.get(0);

        // Find by ID
        RestAssured.given()
                .param("id", first.getId())
                .get("/item")
                .then()
                .statusCode(200);

        // Find by name
        RestAssured.given()
                .param("name", first.getName())
                .get("/item")
                .then()
                .statusCode(200);

        // Find with pagination
        RestAssured.given()
                .param("pnumber", 0)
                .param("psize", 2)
                .get("/item")
                .then()
                .statusCode(200);
    }

    @Test
    void testDelete() {
        Item item = items.get(0);

        RestAssured.given()
                .param("id", item.getId())
                .delete("/item")
                .then()
                .statusCode(204);

        assertFalse(itemRepository.existsById(item.getId()));
    }

    @Test
    void testFindAllWithPagination() {
        RestAssured.given()
                .param("pnumber", 0)
                .param("psize", 3)
                .get("/item")
                .then()
                .statusCode(200)
                .header("X-Total-Count", "5");
    }

    @Test
    void testCreateDuplicateItem() {
        Gson gson = new Gson();
        Item existingItem = items.get(0);

        ItemCreateDto dto = new ItemCreateDto(
                existingItem.getName(),
                400, 25, 15, 60
        );

        RestAssured.given()
                .contentType("application/json")
                .body(gson.toJson(dto))
                .post("/item")
                .then()
                .statusCode(400);
    }

    @Test
    void testUpdateNonExistingItem() {
        Gson gson = new Gson();

        ItemUpdateDto dto = new ItemUpdateDto(
                999999L,
                "NON_EXISTENT",
                500, 30, 20, 70
        );

        RestAssured.given()
                .contentType("application/json")
                .body(gson.toJson(dto))
                .put("/item")
                .then()
                .statusCode(404);
    }

    @Test
    void testDeleteNonExistingItem() {
        RestAssured.given()
                .param("id", 999999L)
                .delete("/item")
                .then()
                .statusCode(404);
    }
}