
import org.itmo.user.accounter.UserAccounterApp;
import org.itmo.user.accounter.model.dto.UserCreateDto;
import org.itmo.user.accounter.model.dto.UserDto;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(classes = UserAccounterApp.class)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class UserControllerTest extends AbstractIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

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
