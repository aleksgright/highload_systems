package secs.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.itmo.secs.App;
import org.itmo.secs.client.DishServiceClient;
import org.itmo.secs.client.UserServiceClient;
import org.itmo.secs.model.dto.*;
import org.itmo.secs.model.entities.enums.Meal;
import org.itmo.secs.repositories.MenuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = App.class)
@AutoConfigureWebTestClient
@Testcontainers
@ActiveProfiles("test")
class MenuControllerTest {
    @LocalServerPort
    private String port;
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private MenuRepository menuRepository;
    @MockitoBean
    private UserServiceClient userServiceClient;
    @MockitoBean
    private DishServiceClient dishServiceClient;
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("password")
            .waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://" + POSTGRES.getHost() + ":" + POSTGRES.getMappedPort(5432) + "/testdb");
        registry.add("spring.r2dbc.username", POSTGRES::getUsername);
        registry.add("spring.r2dbc.password", POSTGRES::getPassword);
        registry.add("spring.flyway.url", POSTGRES::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRES::getUsername);
        registry.add("spring.flyway.password", POSTGRES::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.flyway.baseline-on-migrate", () -> true);
        registry.add("eureka.client.enabled", () -> false);
        registry.add("spring.cloud.config.enabled", () -> false);
        registry.add("spring.cloud.loadbalancer.enabled", () -> false);
        registry.add("app.max-page-size", () -> "10");
        registry.add("app.default-page-size", () -> "5");
    }

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    private MenuCreateDto createDto;

    @BeforeEach
    void setup() {
        // Чистим репозиторий перед каждым тестом
        menuRepository.deleteAll().block();
        // Моки для пользователя и блюда
        UserDto testUser = new UserDto(1L, "TestUser");
        when(userServiceClient.getById(anyLong())).thenReturn(Mono.just(testUser));
        when(userServiceClient.getByName(anyString())).thenReturn(Mono.just(testUser));
        DishDto testDish = new DishDto(100L, "Test Dish", 100, 20, 10, 5);
        when(dishServiceClient.getById(anyLong())).thenReturn(Mono.just(testDish));
        createDto = new MenuCreateDto(
                "BREAKFAST",
                1L,
                LocalDate.now()
        );
    }

    // =========================
    // Helper methods
    // =========================

    private MenuDto createMenu(MenuCreateDto dto) {
        return webTestClient.post()
                .uri("/menu")
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(MenuDto.class)
                .returnResult()
                .getResponseBody();
    }

    private MenuDto getMenuById(Long id) throws Exception {
        String json = webTestClient.get()
                .uri("/menu?id=" + id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        return parseMenu(json);
    }

    private List<MenuDto> getMenusByUsername(String username) throws Exception {
        String json = webTestClient.get()
                .uri("/menu?username=" + username)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        return parseMenuList(json);
    }

    private List<MenuDto> getMenusWithPaging(int page, int size) throws Exception {
        String json = webTestClient.get()
                .uri("/menu?pnumber=" + page + "&psize=" + size)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        return parseMenuList(json);
    }

    private List<MenuDto> getAllMenus() throws Exception {
        String json = webTestClient.get()
                .uri("/menu")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        return parseMenuList(json);
    }

    private void updateMenu(Long id, MenuUpdateDto dto) {
        webTestClient.put()
                .uri("/menu")
                .bodyValue(dto)
                .exchange()
                .expectStatus().isNoContent();
    }

    private void deleteMenu(Long id) {
        webTestClient.delete()
                .uri("/menu/" + id)
                .exchange()
                .expectStatus().isNoContent();
    }

    private MenuDto parseMenu(String json) throws Exception {
        return mapper.readValue(json, MenuDto.class);
    }

    private List<MenuDto> parseMenuList(String json) throws Exception {
        return mapper.readValue(
                json,
                mapper.getTypeFactory().constructCollectionType(List.class, MenuDto.class)
        );
    }

    // =========================
    // Tests
    // =========================

    @Test
    void createMenu_success() {
        MenuCreateDto request = new MenuCreateDto(Meal.LUNCH.toString(), 1L, LocalDate.of(2024, 1, 16));
        MenuDto created = webTestClient.post().uri("/menu").contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isCreated().expectBody(MenuDto.class).returnResult().getResponseBody();
        assert created != null;
        assert created.id() != null;
        assert created.meal().equals("LUNCH");
        assert created.date().equals(LocalDate.of(2024, 1, 16));
    }

    @Test
    void createMenu_duplicateKey_400() {
        MenuCreateDto request = new MenuCreateDto(Meal.BREAKFAST.toString(), 1L, LocalDate.of(2024, 1, 15));
        // Создаем первый раз
        webTestClient.post().uri("/menu").contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isCreated();
        // Пытаемся создать дубликат
        webTestClient.post().uri("/menu").contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isBadRequest();
    }

    @Test
    void findMenuById_success() throws Exception {
        MenuDto created = createMenu(createDto);

        MenuDto found = getMenuById(created.id());

        assertThat(found.id()).isEqualTo(created.id());
        assertThat(found.meal()).isEqualTo("BREAKFAST");
    }

    @Test
    void findMenusByUsername_success() throws Exception {
        createMenu(createDto);

        List<MenuDto> menus = getMenusByUsername("TestUser");

        assertThat(menus).isNotEmpty();
    }

//    @Test
//    void findAllMenusWithPagination_success() throws Exception {
//        MenuCreateDto testCreateDto = new MenuCreateDto(
//                "BREAKFAST",
//                1L,
//                LocalDate.now()
//        );
//        createMenu(createDto);
//        createMenu(createDto);
//
//        List<MenuDto> menus = getMenusWithPaging(0, 2);
//
//        assertThat(menus).hasSize(2);
//    }

    @Test
    void updateMenu_success() throws Exception {
        MenuDto created = createMenu(createDto);

        MenuUpdateDto updateDto = new MenuUpdateDto(
                created.id(),
                LocalDate.now(),
                "DINNER"
        );

        updateMenu(created.id(), updateDto);

        MenuDto updated = getMenuById(created.id());

        assertThat(updated.meal()).isEqualTo("DINNER");
    }

    @Test
    void deleteMenu_success() {
        MenuDto created = webTestClient.post().uri("/menu").contentType(MediaType.APPLICATION_JSON).bodyValue(new MenuCreateDto(Meal.BREAKFAST.toString(), 1L, LocalDate.of(2024, 1, 15))).exchange().expectStatus().isCreated().expectBody(MenuDto.class).returnResult().getResponseBody();
        webTestClient.delete()
                .uri("/menu?id=" + created.id())
                .exchange().expectStatus().isNoContent();
        webTestClient.get().uri("/menu?id=" + created.id()).exchange().expectStatus().isNotFound();
    }

    @Test
    void deleteMenu_notFound_404() {
        webTestClient.delete().uri("/menu?id=99999")
                .exchange().expectStatus().isNotFound();
    }

    @Test
    void findMenu_withoutParameters_returnsEmptyList() throws Exception {
        List<MenuDto> menus = getAllMenus();

        assertThat(menus).isEmpty();
    }

    @Test
    void addDish_dishNotFound_shouldReturn404() {
        // Given
        MenuDto menu = createMenu(createDto);

        // Mock dishServiceClient to return error for non-existent dish
        when(dishServiceClient.getById(99999L))
                .thenReturn(Mono.error(new org.itmo.secs.utils.exceptions.ItemNotFoundException("Dish not found")));

        MenuDishDto addDishRequest = new MenuDishDto(menu.id(), 99999L);

        // When & Then
        webTestClient.put()
                .uri("/menu/dishes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(addDishRequest)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorDto.class)
                .consumeWith(response -> {
                    ErrorDto error = response.getResponseBody();
                    assert error != null;
                    assert error.message().contains("Dish with id 99999 was not found");
                });
    }
}
