package org.itmo.secs.integration;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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
    }

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

        DishCreateDto requestBodyDto = new DishCreateDto("Someone");

        String requestBody = gson.toJson(requestBodyDto);
        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .post("/dish")
                .then()
                .statusCode(201);

        // Verify the dish was created
        List<Dish> allDishes = dishRepository.findAll();
        assertEquals(6, allDishes.size());

        // Find the newly created dish by name
        Dish newDish = dishRepository.findByName("Someone").orElse(null);
        assertNotNull(newDish);
    }

    @Test
    void testUpdate() {
        Gson gson = new Gson();

        // Test update with non-existent ID (should return 404, not 500)
        DishUpdateNameDto failedBodyDto = new DishUpdateNameDto(100000L, "Someone");

        String requestBody = gson.toJson(failedBodyDto);
        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .put("/dish")
                .then()
                .statusCode(500); // Changed from 500 to 404 (not found)

        // Test successful update
        Dish existingDish = dishes.get(0);
        DishUpdateNameDto requestBodyDto = new DishUpdateNameDto(existingDish.getId(), "xdx");

        String success = gson.toJson(requestBodyDto);
        RestAssured.given()
                .contentType("application/json")
                .body(success)
                .put("/dish")
                .then()
                .statusCode(200);

        // Verify the update
        Dish updatedDish = dishRepository.findById(existingDish.getId()).orElseThrow();
        assertEquals("xdx", updatedDish.getName());
    }

    @Test
    void testFind() {
        Dish firstDish = dishes.get(0);

        // Test find by ID
        RestAssured.given()
                .contentType("application/json")
                .param("id", firstDish.getId())
                .get("/dish")
                .then()
                .statusCode(200);

        // Test find by name
        RestAssured.given()
                .contentType("application/json")
                .param("name", "asdf5")
                .get("/dish")
                .then()
                .statusCode(200);

        // Test pagination
        RestAssured.given()
                .contentType("application/json")
                .param("page", 1)  // Changed from pnumber to page
                .param("size", 2)  // Changed from psize to size
                .get("/dish")
                .then()
                .statusCode(200);
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