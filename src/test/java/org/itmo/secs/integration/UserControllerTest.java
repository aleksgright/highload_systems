package org.itmo.secs.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.itmo.secs.model.entities.User;
import org.itmo.secs.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static io.restassured.RestAssured.given;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserControllerTest {

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
    private UserRepository userRepository;

    private String uniqueSuffix;

    @BeforeEach
    void setUp() {
        uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        userRepository.deleteAllInBatch();

        RestAssured.baseURI = "http://localhost:" + port;
        RestAssured.port = Integer.parseInt(port);
    }

    private String createUserJson(String name) {
        return String.format("{\"name\":\"%s\"}", name);
    }

    private String createUserDtoJson(Long id, String name) {
        return String.format("{\"id\":%d,\"name\":\"%s\"}", id, name);
    }

    @Test
    void testCreateUser() {
        String userName = "Test User " + uniqueSuffix;
        String requestBody = createUserJson(userName);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/user")
                .then()
                .extract().response();

        assertEquals(201, response.statusCode());

        User savedUser = userRepository.findByName(userName).orElse(null);
        assertNotNull(savedUser);
        assertEquals(userName, savedUser.getName());
    }

    @Test
    void testCreateUserWithDuplicateName() {
        String userName = "Duplicate User " + uniqueSuffix;

        User existingUser = new User();
        existingUser.setName(userName);
        userRepository.save(existingUser);

        String requestBody = createUserJson(userName);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/user")
                .then()
                .extract().response();

        assertEquals(400, response.statusCode());
    }

    @Test
    void testCreateUserWithEmptyName() {
        String requestBody = createUserJson("");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/user")
                .then()
                .extract().response();

        assertEquals(201, response.statusCode());
    }

    @Test
    void testUpdateUser() {
        User existingUser = new User();
        existingUser.setName("Original Name " + uniqueSuffix);
        User savedUser = userRepository.save(existingUser);

        String newName = "Updated Name " + uniqueSuffix;
        String requestBody = createUserDtoJson(savedUser.getId(), newName);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .put("/user")
                .then()
                .extract().response();

        assertEquals(200, response.statusCode());

        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertEquals(newName, updatedUser.getName());
    }

    @Test
    void testUpdateNonExistingUser() {
        String requestBody = createUserDtoJson(999999L, "Non Existent");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .put("/user")
                .then()
                .extract().response();

        assertEquals(404, response.statusCode());
    }

    @Test
    void testUpdateUserToDuplicateName() {
        User user1 = new User();
        user1.setName("User 1 " + uniqueSuffix);
        User savedUser1 = userRepository.save(user1);

        User user2 = new User();
        user2.setName("User 2 " + uniqueSuffix);
        userRepository.save(user2);

        String requestBody = createUserDtoJson(savedUser1.getId(), "User 2 " + uniqueSuffix);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .put("/user")
                .then()
                .extract().response();

        assertEquals(500, response.statusCode());
    }

    @Test
    void testDeleteUser() {
        User user = new User();
        user.setName("User to Delete " + uniqueSuffix);
        User savedUser = userRepository.save(user);

        Response response = given()
                .param("id", savedUser.getId())
                .delete("/user")
                .then()
                .extract().response();

        assertEquals(204, response.statusCode());
        assertFalse(userRepository.existsById(savedUser.getId()));
    }

    @Test
    void testDeleteNonExistingUser() {
        Response response = given()
                .param("id", 999999L)
                .delete("/user")
                .then()
                .extract().response();

        assertEquals(404, response.statusCode());
    }

    @Test
    void testFindUserById() {
        User user = new User();
        user.setName("Find by ID " + uniqueSuffix);
        User savedUser = userRepository.save(user);

        Response response = given()
                .param("id", savedUser.getId())
                .get("/user")
                .then()
                .extract().response();

        assertEquals(200, response.statusCode());
        String responseBody = response.getBody().asString();
        assertTrue(responseBody.contains("Find by ID"));
    }

    @Test
    void testFindUserByNonExistingId() {
        Response response = given()
                .param("id", 999999L)
                .get("/user")
                .then()
                .extract().response();

        assertEquals(404, response.statusCode());
    }

    @Test
    void testFindUserByName() {
        String userName = "Find by Name " + uniqueSuffix;
        User user = new User();
        user.setName(userName);
        userRepository.save(user);

        Response response = given()
                .param("name", userName)
                .get("/user")
                .then()
                .extract().response();

        assertEquals(200, response.statusCode());
        String responseBody = response.getBody().asString();
        assertTrue(responseBody.contains(userName));
    }

    @Test
    void testFindUserByNonExistingName() {
        Response response = given()
                .param("name", "Non Existing Name")
                .get("/user")
                .then()
                .extract().response();

        assertEquals(404, response.statusCode());
    }

    @Test
    void testFindUserWithoutParameters() {
        Response response = given()
                .get("/user")
                .then()
                .extract().response();

        assertEquals(400, response.statusCode());
    }

    @Test
    void testFindUserWithBothIdAndName() {
        User user = new User();
        user.setName("Test User " + uniqueSuffix);
        User savedUser = userRepository.save(user);

        Response response = given()
                .param("id", savedUser.getId())
                .param("name", "Different Name")
                .get("/user")
                .then()
                .extract().response();

        assertEquals(200, response.statusCode());
        String responseBody = response.getBody().asString();
        assertTrue(responseBody.contains("Test User"));
    }

    @Test
    void testUpdateUserWithSameName() {
        String userName = "Same Name " + uniqueSuffix;
        User user = new User();
        user.setName(userName);
        User savedUser = userRepository.save(user);

        String requestBody = createUserDtoJson(savedUser.getId(), userName);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .put("/user")
                .then()
                .extract().response();

        assertEquals(200, response.statusCode());

        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertEquals(userName, updatedUser.getName());
    }

    @Test
    void testCreateMultipleUsers() {
        for (int i = 1; i <= 3; i++) {
            String userName = "User " + i + " " + uniqueSuffix;
            String requestBody = createUserJson(userName);

            Response response = given()
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .post("/user")
                    .then()
                    .extract().response();

            assertEquals(201, response.statusCode());
        }

        assertEquals(3, userRepository.count());
    }

    @Test
    void testDeleteAndRecreateUser() {
        String userName = "Recreate User " + uniqueSuffix;

        String requestBody = createUserJson(userName);
        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/user")
                .then()
                .extract().response();

        assertEquals(201, createResponse.statusCode());

        String responseBody = createResponse.getBody().asString();
        Long userId = extractUserId(responseBody);

        Response deleteResponse = given()
                .param("id", userId)
                .delete("/user")
                .then()
                .extract().response();

        assertEquals(204, deleteResponse.statusCode());

        Response recreateResponse = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/user")
                .then()
                .extract().response();

        assertEquals(201, recreateResponse.statusCode());
    }

    @Test
    void testFindUserWithNullName() {
        Response response = given()
                .param("name", (String) null)
                .get("/user")
                .then()
                .extract().response();

        assertEquals(404, response.statusCode());
    }

    private Long extractUserId(String jsonResponse) {
        try {
            String idPart = jsonResponse.split("\"id\":")[1].split(",")[0].trim();
            return Long.parseLong(idPart);
        } catch (Exception e) {
            return null;
        }
    }
}