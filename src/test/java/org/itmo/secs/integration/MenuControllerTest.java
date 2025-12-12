package org.itmo.secs.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.itmo.secs.model.entities.Dish;
import org.itmo.secs.model.entities.Menu;
import org.itmo.secs.model.entities.User;
import org.itmo.secs.model.entities.enums.Meal;
import org.itmo.secs.repositories.DishRepository;
import org.itmo.secs.repositories.MenuRepository;
import org.itmo.secs.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static io.restassured.RestAssured.given;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MenuControllerTest {

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
    private MenuRepository menuRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DishRepository dishRepository;

    private User testUser;
    private Dish testDish;
    private Menu testMenu;

    @BeforeEach
    void setUp() {
        menuRepository.deleteAll();
        dishRepository.deleteAll();
        userRepository.deleteAll();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        RestAssured.baseURI = "http://localhost:" + port;
        RestAssured.port = Integer.parseInt(port);

        testUser = new User();
        testUser.setName("Test User");
        testUser = userRepository.save(testUser);

        testDish = new Dish();
        testDish.setName("Test Dish");
        testDish = dishRepository.save(testDish);

        testMenu = new Menu();
        testMenu.setMeal(Meal.BREAKFAST);
        testMenu.setDate(LocalDate.of(2024, 1, 15));
        testMenu.setUser(testUser);
        testMenu.setDishes(new ArrayList<>());
        testMenu = menuRepository.save(testMenu);

        for (int i = 1; i <= 5; i++) {
            Menu menu = new Menu();
            menu.setMeal(Meal.values()[i % Meal.values().length]);
            menu.setDate(LocalDate.of(2024, 1, 15 + i));
            menu.setUser(i % 2 == 0 ? testUser : null);
            menu.setDishes(new ArrayList<>());
            menuRepository.save(menu);
        }
    }

    private String createMenuJson(String meal, Long userId, String date) {
        if (userId != null) {
            return String.format("{\"meal\":\"%s\",\"userId\":%d,\"date\":\"%s\"}", meal, userId, date);
        }
        return String.format("{\"meal\":\"%s\",\"userId\":null,\"date\":\"%s\"}", meal, date);
    }

    private String createMenuDtoJson(Long id, String meal, Long userId, String date) {
        return String.format("{\"id\":%d,\"meal\":\"%s\",\"userId\":%d,\"date\":\"%s\"}", id, meal, userId, date);
    }

    private String createMenuDishDtoJson(Long menuId, Long dishId) {
        return String.format("{\"menuId\":%d,\"dishId\":%d}", menuId, dishId);
    }

    @Test
    void testCreateMenu() {
        String requestBody = createMenuJson("LUNCH", testUser.getId(), "2024-01-20");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/menu")
                .then()
                .extract().response();

        assertEquals(201, response.statusCode());

        Menu savedMenu = menuRepository.findByMealAndDateAndUserId(
                Meal.LUNCH,
                LocalDate.of(2024, 1, 20),
                testUser.getId()
        ).orElse(null);
        assertNotNull(savedMenu);
    }

    @Test
    void testCreateGlobalMenu() {
        String requestBody = createMenuJson("LUNCH", null, "2024-01-20");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/menu")
                .then()
                .extract().response();

        assertEquals(201, response.statusCode());

        Menu savedMenu = menuRepository.findByMealAndDateAndUserId(
                Meal.LUNCH,
                LocalDate.of(2024, 1, 20),
                null
        ).orElse(null);
        assertNotNull(savedMenu);
        assertNull(savedMenu.getUser());
    }

    @Test
    void testCreateDuplicateMenu() {
        String requestBody = createMenuJson("BREAKFAST", testUser.getId(), "2024-01-15");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/menu")
                .then()
                .extract().response();

        assertEquals(400, response.statusCode());
    }

    @Test
    void testUpdateMenu() {
        String requestBody = createMenuDtoJson(testMenu.getId(), "DINNER", testUser.getId(), "2024-01-25");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .put("/menu")
                .then()
                .extract().response();

        assertEquals(204, response.statusCode());

        Menu updatedMenu = menuRepository.findById(testMenu.getId()).orElseThrow();
        assertEquals(Meal.DINNER, updatedMenu.getMeal());
        assertEquals(LocalDate.of(2024, 1, 25), updatedMenu.getDate());
    }

    @Test
    void testUpdateNonExistingMenu() {
        String requestBody = createMenuDtoJson(999999L, "DINNER", testUser.getId(), "2024-01-20");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .put("/menu")
                .then()
                .extract().response();

        assertEquals(404, response.statusCode());
    }

    @Test
    void testDeleteMenu() {
        Long menuId = testMenu.getId();

        Response response = given()
                .param("id", menuId)
                .delete("/menu")
                .then()
                .extract().response();

        assertEquals(204, response.statusCode());
        assertFalse(menuRepository.existsById(menuId));
    }

    @Test
    void testDeleteNonExistingMenu() {
        Response response = given()
                .param("id", 999999L)
                .delete("/menu")
                .then()
                .extract().response();

        assertEquals(404, response.statusCode());
    }

    @Test
    void testFindMenuById() {
        Response response = given()
                .param("id", testMenu.getId())
                .get("/menu")
                .then()
                .extract().response();

        assertEquals(200, response.statusCode());
        assertTrue(response.getBody().asString().contains("BREAKFAST"));
    }

    @Test
    void testFindNonExistingMenuById() {
        Response response = given()
                .param("id", 999999L)
                .get("/menu")
                .then()
                .extract().response();

        assertEquals(404, response.statusCode());
    }

    @Test
    void testFindAllMenus() {
        Response response = given()
                .get("/menu")
                .then()
                .extract().response();

        assertEquals(200, response.statusCode());
        String responseBody = response.getBody().asString();
        assertTrue(responseBody.contains("BREAKFAST") || responseBody.contains("LUNCH") || responseBody.contains("DINNER") || responseBody.contains("SUPPER"));
    }

    @Test
    void testFindAllMenusWithPagination() {
        Response response = given()
                .param("pnumber", 0)
                .param("psize", 2)
                .get("/menu")
                .then()
                .extract().response();

        assertEquals(200, response.statusCode());
    }



    @Test
    void testAddDishToNonExistingMenu() {
        String requestBody = createMenuDishDtoJson(999999L, testDish.getId());

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .put("/menu/dishes")
                .then()
                .extract().response();

        assertEquals(404, response.statusCode());
    }

    @Test
    void testAddNonExistingDishToMenu() {
        String requestBody = createMenuDishDtoJson(testMenu.getId(), 999999L);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .put("/menu/dishes")
                .then()
                .extract().response();

        assertEquals(404, response.statusCode());
    }

    @Test
    void testGetDishesFromMenu() {
        testMenu.getDishes().add(testDish);
        menuRepository.save(testMenu);

        Dish anotherDish = new Dish();
        anotherDish.setName("Another Dish");
        anotherDish = dishRepository.save(anotherDish);
        testMenu.getDishes().add(anotherDish);
        menuRepository.save(testMenu);

        Response response = given()
                .param("id", testMenu.getId())
                .get("/menu/dishes")
                .then()
                .extract().response();

        assertEquals(200, response.statusCode());
        String responseBody = response.getBody().asString();
        assertTrue(responseBody.contains("Test Dish"));
        assertTrue(responseBody.contains("Another Dish"));
    }

    @Test
    void testGetDishesFromEmptyMenu() {
        Response response = given()
                .param("id", testMenu.getId())
                .get("/menu/dishes")
                .then()
                .extract().response();

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.getBody().asString());
    }

    @Test
    void testGetDishesFromNonExistingMenu() {
        Response response = given()
                .param("id", 999999L)
                .get("/menu/dishes")
                .then()
                .extract().response();

        assertEquals(404, response.statusCode());
    }

    @Test
    @Transactional
    void testDeleteDishFromNonExistingMenu() {
        String requestBody = createMenuDishDtoJson(999999L, testDish.getId());

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .delete("/menu/dishes")
                .then()
                .extract().response();

        assertEquals(404, response.statusCode());
    }

    @Test
    void testDeleteNonExistingDishFromMenu() {
        String requestBody = createMenuDishDtoJson(testMenu.getId(), 999999L);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .delete("/menu/dishes")
                .then()
                .extract().response();

        assertEquals(404, response.statusCode());
    }

    @Test
    void testDeleteDishNotInMenu() {
        Dish anotherDish = new Dish();
        anotherDish.setName("Another Dish");
        anotherDish = dishRepository.save(anotherDish);

        String requestBody = createMenuDishDtoJson(testMenu.getId(), anotherDish.getId());

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .delete("/menu/dishes")
                .then()
                .extract().response();

        assertEquals(204, response.statusCode());
    }

    @Test
    void testFindMenusWithZeroPageSize() {
        Response response = given()
                .param("pnumber", 0)
                .param("psize", 0)
                .get("/menu")
                .then()
                .extract().response();

        assertEquals(500, response.statusCode());
    }

    @Test
    void testUpdateMenuToDuplicateKey() {
        Menu anotherMenu = new Menu();
        anotherMenu.setMeal(Meal.LUNCH);
        anotherMenu.setDate(LocalDate.of(2024, 1, 16));
        anotherMenu.setUser(testUser);
        anotherMenu.setDishes(new ArrayList<>());
        anotherMenu = menuRepository.save(anotherMenu);

        String requestBody = createMenuDtoJson(testMenu.getId(), "LUNCH", testUser.getId(), "2024-01-16");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .put("/menu")
                .then()
                .extract().response();

        assertEquals(400, response.statusCode());
    }

    @Test
    void testCreateMenuWithNonExistingUser() {
        String requestBody = createMenuJson("LUNCH", 999999L, "2024-01-20");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/menu")
                .then()
                .extract().response();

        assertTrue(response.statusCode() >= 400 && response.statusCode() < 600);
    }
}