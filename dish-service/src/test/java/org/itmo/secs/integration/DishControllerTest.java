package org.itmo.secs.integration;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import org.itmo.secs.model.dto.DishAddItemDto;
import org.itmo.secs.model.dto.DishCreateDto;
import org.itmo.secs.model.dto.DishUpdateNameDto;
import org.itmo.secs.model.entities.Dish;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.restassured.RestAssured;

import static org.junit.jupiter.api.Assertions.*;

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
        // Отключаем Liquibase для тестов
        registry.add("spring.liquibase.enabled", () -> "false");
        // Отключаем конфиг сервер для тестов
        registry.add("spring.cloud.config.enabled", () -> "false");
        registry.add("spring.cloud.config.import-check.enabled", () -> "false");
        registry.add("app.max-page-size", () -> "10");
        registry.add("app.default-page-size", () -> "5");
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private DishRepository dishRepository;

    private List<Dish> dishes;
    private List<Item> items;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        dishRepository.deleteAll();

        RestAssured.baseURI = "http://localhost:" + port;
        RestAssured.port = Integer.parseInt(port);

        // Create items
        items = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Item item = new Item();
            item.setName("Milk" + i);
            item.setCalories(300);
            item.setProtein(20);
            item.setFats(10);
            item.setCarbs(50);
            items.add(item);
        }
        items = itemRepository.saveAll(items);

        // Create dishes
        dishes = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Dish dish = new Dish();
            dish.setName("asdf" + i);
            dish.setItems_dishes(new ArrayList<>());
            dishes.add(dish);
        }
        dishes = dishRepository.saveAll(dishes);

        assertFalse(dishes.isEmpty());
    }

    @Test
    void testCreateNewDish() {
        Gson gson = new Gson();

        DishCreateDto dto = new DishCreateDto("NEW_DISH");

        webTestClient.post()
                .uri("/dish")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.name").isEqualTo(dto.name());

        assertTrue(dishRepository.findByName("NEW_DISH").isPresent());
    }

    @Test
    void testUpdate() {
        Gson gson = new Gson();

        DishUpdateNameDto dto =
                new DishUpdateNameDto(dishes.get(0).getId(), "NEW_NAME");

        webTestClient.put()
                .uri("/dish")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isNoContent();

        assertEquals(
                "NEW_NAME",
                dishRepository.findById(dishes.get(0).getId()).orElseThrow().getName()
        );
    }

    @Test
    void testFindById() {
        Dish first = dishes.get(0);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/dish")
                        .queryParam("id", first.getId())
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(first.getId().toString())
                .jsonPath("$.name").isEqualTo(first.getName());
    }

    @Test
    void testFindByName() {
        Dish first = dishes.get(0);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/dish")
                        .queryParam("name", first.getName())
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(first.getId().toString())
                .jsonPath("$.name").isEqualTo(first.getName());
    }

    @Test
    void testFindAllWithPagination() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/dish")
                        .queryParam("pnumber", 0)
                        .queryParam("psize", 2)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2) // Проверяем длину массива
                .jsonPath("$[0].id").isEqualTo(dishes.get(0).getId().toString())
                .jsonPath("$[0].name").isEqualTo(dishes.get(0).getName())
                .jsonPath("$[1].id").isEqualTo(dishes.get(1).getId().toString())
                .jsonPath("$[1].name").isEqualTo(dishes.get(1).getName());
    }

    @Test
    void testDelete() {
        Dish dish = dishes.get(0);

        webTestClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/dish")
                        .queryParam("id", dish.getId())
                        .build())
                .exchange()
                .expectStatus().isNoContent();

        assertFalse(dishRepository.existsById(dish.getId()));
    }

    @Test
    void testAddItem() {
        Gson gson = new Gson();

        Dish dish = dishes.get(0);
        Item item = items.get(0);

        DishAddItemDto dto = new DishAddItemDto(item.getId(), dish.getId(), 50);

        webTestClient.put()
                .uri("/dish/items")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isNoContent();
    }
//    @Test
//    void testDelete() {
//        Dish dishToDelete = dishes.get(0);
//
//        // Test delete
//        RestAssured.given()
//                .contentType("application/json")
//                .param("id", dishToDelete.getId())
//                .delete("/dish")
//                .then()
//                .statusCode(200);
//
//        // Verify deletion
//        assertFalse(dishRepository.existsById(dishToDelete.getId()));
//    }
//
//    @Test
//    void testGetAllDishes() {
//        RestAssured.given()
//                .contentType("application/json")
//                .get("/dish/all")
//                .then()
//                .statusCode(200);
//    }
}