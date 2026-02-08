
import org.itmo.user.accounter.UserAccounterApp;
import org.itmo.user.accounter.model.dto.UserCreateDto;
import org.itmo.user.accounter.model.dto.UserDto;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = UserAccounterApp.class)
@AutoConfigureWebTestClient
@Testcontainers
@ActiveProfiles("test")
class UserControllerTest {
    @LocalServerPort
    private String port;

    @Autowired
    private WebTestClient webTestClient;


//    @BeforeEach
//    void setup() {
//        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
//    }
    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("postgres")
                    .withPassword("password")
                    .waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        //r2dbc props
        String jdbcUrl = POSTGRES.getJdbcUrl();
        registry.add("spring.r2dbc.url",
                () -> "r2dbc:postgresql://" + POSTGRES.getHost() + ":" +
                        POSTGRES.getMappedPort(5432) +
                        "/testdb");
        registry.add("spring.r2dbc.username", POSTGRES::getUsername);
        registry.add("spring.r2dbc.password", POSTGRES::getPassword);
        // jdbc liquibase props
        registry.add("spring.flyway.url", POSTGRES::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRES::getUsername);
        registry.add("spring.flyway.password", POSTGRES::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.flyway.baseline-on-migrate", () -> true);
    }

    @BeforeAll
    static void beforeAll() {
        // Initialize schema manually
        try (Connection conn = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword());
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id BIGSERIAL PRIMARY KEY,
                    name VARCHAR(255) NOT NULL UNIQUE
                )
                """);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create table", e);
        }
    }
    /* ---------------- CREATE ---------------- */

    @Test
    void createUser_success() {
        UserCreateDto request = new UserCreateDto("Alex");

        webTestClient.post()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserDto.class)
                .value(dto -> {
                    assert dto.id() != null;
                    assert dto.name().equals("Alex");
                });
    }

    @Test
    void createUser_duplicateName_400() {
        UserCreateDto request = new UserCreateDto("Duplicate");

        webTestClient.post()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    /* ---------------- FIND ---------------- */

    @Test
    void findUserById_success() {
        UserDto created = webTestClient.post()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserCreateDto("FindMe"))
                .exchange()
                .expectBody(UserDto.class)
                .returnResult()
                .getResponseBody();

        webTestClient.get()
                .uri(uri -> uri.path("/user")
                        .queryParam("id", created.id())
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .isEqualTo(created);
    }

    @Test
    void findUser_badRequest() {
        webTestClient.get()
                .uri("/user")
                .exchange()
                .expectStatus().isBadRequest();
    }

    /* ---------------- UPDATE ---------------- */

    @Test
    void updateUser_success() {
        UserDto created = webTestClient.post()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserCreateDto("OldName"))
                .exchange()
                .expectBody(UserDto.class)
                .returnResult()
                .getResponseBody();

        UserDto updated = new UserDto(created.id(), "NewName");

        webTestClient.put()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updated)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri(uri -> uri.path("/user")
                        .queryParam("id", created.id())
                        .build())
                .exchange()
                .expectBody(UserDto.class)
                .value(dto -> dto.name().equals("NewName"));
    }

    /* ---------------- DELETE ---------------- */

    @Test
    void deleteUser_success() {
        UserDto created = webTestClient.post()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserCreateDto("ToDelete"))
                .exchange()
                .expectBody(UserDto.class)
                .returnResult()
                .getResponseBody();

        webTestClient.delete()
                .uri(uri -> uri.path("/user")
                        .queryParam("id", created.id())
                        .build())
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri(uri -> uri.path("/user")
                        .queryParam("id", created.id())
                        .build())
                .exchange()
                .expectStatus().isNotFound();
    }
}
