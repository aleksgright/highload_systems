package org.itmo.secs.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import io.restassured.RestAssured;

import org.itmo.secs.model.dto.ItemCreateDto;
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

import com.google.gson.Gson;

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

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        RestAssured.baseURI = "http://localhost:" + port;

        List<Item> items = List.of(
                new Item(1L, 300, "Milk1", 20, 10, 50, 0L),
                new Item(2L, 300, "Milk2", 20, 10, 50, 0L),
                new Item(3L, 300, "Milk3", 20, 10, 50, 0L),
                new Item(4L, 300, "Milk4", 20, 10, 50, 0L),
                new Item(5L, 300, "Milk5", 20, 10, 50, 0L));

        itemRepository.saveAll(items);
    }

    @Test
    void testCreateNewItem() {
        Gson gson = new Gson();

        ItemCreateDto requestBodyDto = new ItemCreateDto();
        requestBodyDto.setCalories(200);
        requestBodyDto.setCarbs(200);
        requestBodyDto.setFats(200);
        requestBodyDto.setName("Someone");
        requestBodyDto.setProtein(200);

        String requestBody = gson.toJson(requestBodyDto);
        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .post("/item/create")
                .then()
                .statusCode(200);

        assertTrue(itemRepository.findByName(requestBodyDto.getName()).isPresent());
    }
}
