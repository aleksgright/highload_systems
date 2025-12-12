package org.itmo.secs.integration;

import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.itmo.secs.model.dto.ItemCreateDto;
import org.itmo.secs.model.dto.ItemUpdateDto;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.repositories.ItemRepository;
import org.junit.Ignore;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static io.restassured.RestAssured.given;

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
    }

    @Autowired
    private ItemRepository itemRepository;

    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        RestAssured.baseURI = "http://localhost:" + port;
        RestAssured.port = Integer.parseInt(port);

        List<Item> items = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Item item = new Item();
            item.setName("Milk" + i);
            item.setCalories(300);
            item.setProtein(20);
            item.setFats(10);
            item.setCarbs(50);
            item.setCreatorId(0L);
            items.add(item);
        }

        itemRepository.saveAll(items);
    }

    @Test
    void testCreateNewItem() {
        ItemCreateDto requestBodyDto = new ItemCreateDto("TestItem", 200, 15, 5, 30);
        String requestBody = gson.toJson(requestBodyDto);

        Response response = given()
                .contentType("application/json")
                .body(requestBody)
                .post("/item")
                .then()
                .extract().response();

        assertEquals(201, response.statusCode());
        assertTrue(itemRepository.findByName("TestItem").isPresent());

        Item createdItem = itemRepository.findByName("TestItem").orElseThrow();
        assertEquals("TestItem", createdItem.getName());
        assertEquals(200, createdItem.getCalories());
        assertEquals(5, createdItem.getProtein());
        assertEquals(30, createdItem.getFats());
        assertEquals(15, createdItem.getCarbs());
    }

    @Test
    void testCreateDuplicateItemShouldFail() {
        ItemCreateDto requestBodyDto = new ItemCreateDto("Milk1", 200, 15, 5, 30);
        String requestBody = gson.toJson(requestBodyDto);

        Response response = given()
                .contentType("application/json")
                .body(requestBody)
                .post("/item")
                .then()
                .extract().response();

        assertEquals(400, response.statusCode()); // Should b 409
    }

    @Test
    void testUpdateExistingItem() {
        Item existingItem = itemRepository.findByName("Milk1").orElseThrow();
        ItemUpdateDto updateDto = new ItemUpdateDto(
                existingItem.getId(),
                "UpdatedMilk",
                250,
                25,
                15,
                40
        );
        String requestBody = gson.toJson(updateDto);

        Response response = given()
                .contentType("application/json")
                .body(requestBody)
                .put("/item")
                .then()
                .extract().response();

        assertEquals(200, response.statusCode());

        Item updatedItem = itemRepository.findById(existingItem.getId()).orElseThrow();
        assertEquals("UpdatedMilk", updatedItem.getName());
        assertEquals(250, updatedItem.getCalories());
        assertEquals(15, updatedItem.getProtein());
        assertEquals(40, updatedItem.getFats());
        assertEquals(25, updatedItem.getCarbs());
    }

    @Test
    void testUpdatingNonExistingItem() {
        ItemUpdateDto requestBodyDto = new ItemUpdateDto(100000L, "Someone", 10, 10, 10, 10);
        String requestBody = gson.toJson(requestBodyDto);

        Response response = given()
                .contentType("application/json")
                .body(requestBody)
                .put("/item")
                .then()
                .extract().response();

        assertEquals(500, response.statusCode());
    }

    @Test
    void testDeleteExistingItem() {
        Item existingItem = itemRepository.findByName("Milk1").orElseThrow();

        Response response = given()
                .param("id", existingItem.getId())
                .delete("/item")
                .then()
                .extract().response();

        assertEquals(200, response.statusCode());
        assertFalse(itemRepository.existsById(existingItem.getId()));
    }

    @Test
    void testDeleteNonExistingItem() {
        Response response = given()
                .param("id", 999999L)
                .delete("/item")
                .then()
                .extract().response();

        assertEquals(404, response.statusCode());
    }

    @Test
    void testFindById() {
        Item existingItem = itemRepository.findByName("Milk1").orElseThrow();

        Response response = given()
                .param("id", existingItem.getId())
                .get("/item")
                .then()
                .extract().response();

        assertEquals(200, response.statusCode());
        assertTrue(response.getBody().asString().contains("Milk1"));
    }

    @Test
    void testFindByIdNonExisting() {
        Response response = given()
                .param("id", 999999L)
                .get("/item")
                .then()
                .extract().response();

        assertEquals(404, response.statusCode());
    }

    @Test
    void testFindByName() {
        Response response = given()
                .param("name", "Milk2")
                .get("/item")
                .then()
                .extract().response();

        assertEquals(200, response.statusCode());
        assertTrue(response.getBody().asString().contains("Milk2"));
    }

    @Test
    void testFindByNameNonExisting() {
        Response response = given()
                .param("name", "NonExistingItem")
                .get("/item")
                .then()
                .extract().response();

        assertEquals(404, response.statusCode());
    }

    @Test
    void testFindAllWithPagination() {
        Response response = given()
                .param("pnumber", 0)
                .param("psize", 2)
                .get("/item")
                .then()
                .extract().response();

        assertEquals(200, response.statusCode());
        assertTrue(response.getBody().asString().contains("Milk1"));
        assertTrue(response.getBody().asString().contains("Milk2"));

        String totalCount = response.getHeader("X-Total-Count");
        assertNotNull(totalCount);
        assertEquals("5", totalCount);
    }

    @Test
    void testFindAllWithDefaultPagination() {
        Response response = given()
                .get("/item")
                .then()
                .extract().response();

        assertEquals(200, response.statusCode());
    }


    @Test
    void testUpdateWithInvalidData() {
        Item existingItem = itemRepository.findByName("Milk1").orElseThrow();
        ItemUpdateDto updateDto = new ItemUpdateDto(
                existingItem.getId(),
                "UpdatedMilk",
                -100,
                -10,
                -5,
                -20
        );
        String requestBody = gson.toJson(updateDto);

        Response response = given()
                .contentType("application/json")
                .body(requestBody)
                .put("/item")
                .then()
                .extract().response();

        assertTrue(response.statusCode() >= 400 && response.statusCode() < 600);
    }

    @Ignore
    @Test
    void testCreateItemWithEmptyName() {
        ItemCreateDto requestBodyDto = new ItemCreateDto("", 200, 15, 5, 30);
        String requestBody = gson.toJson(requestBodyDto);

        Response response = given()
                .contentType("application/json")
                .body(requestBody)
                .post("/item")
                .then()
                .extract().response();

        assertTrue(response.statusCode() >= 400);
    }

    @Test
    void testFindWithNegativePageNumber() {
        Response response = given()
                .param("pnumber", -1)
                .param("psize", 10)
                .get("/item")
                .then()
                .extract().response();

        assertEquals(500, response.statusCode());
    }

    @Test
    void testFindWithZeroPageSize() {
        Response response = given()
                .param("pnumber", 0)
                .param("psize", 0)
                .get("/item")
                .then()
                .extract().response();

        assertEquals(500, response.statusCode());
    }
}