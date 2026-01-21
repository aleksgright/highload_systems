package org.itmo.secs.controllers;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.itmo.secs.model.dto.*;
import org.itmo.secs.model.entities.Menu;
import org.itmo.secs.services.*;
import org.itmo.secs.utils.conf.PagingConf;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import lombok.AllArgsConstructor;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.Objects;

@AllArgsConstructor
@RestController
@RequestMapping(value = "menu")
@Tag(name = "Меню (Menus API)")
public class MenuController {
    private MenuService menuService;
    private ConversionService conversionService;
    private final JsonConvService jsonConvService;
    private final PagingConf pagingConf;

    @Operation(summary = "Создать новое меню", description = "Создается новое меню по отправленному DTO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Меню было успешно создано",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ItemDto.class))
                }
            ), 
            @ApiResponse(responseCode = "400", description = "Меню с такой же комбинацией ДАТА-ПОЛЬЗОВАТЕЛЬ-ПРИЕМ ПИЩИ уже есть базе",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
                }
            )
        })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MenuDto> create(@RequestBody MenuCreateDto menuDto) {
        return menuService.save(Objects.requireNonNull(conversionService.convert(menuDto, Menu.class)))
                .map((menu) -> Objects.requireNonNull(conversionService.convert(menu, MenuDto.class)));
    }

    @Operation(summary = "Изменить меню", description = "Изменяет меню из БД по отправленному DTO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Успешно изменено"), 
            @ApiResponse(responseCode = "400", description = "Меню с такой же комбинацией ДАТА-ПОЛЬЗОВАТЕЛЬ-ПРИЕМ ПИЩИ уже есть базе",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
                }
            ),
            @ApiResponse(responseCode = "404", description = "Меню с id из DTO не было найдено",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
                }
            )
        })
    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> update(@RequestBody MenuDto menuDto) {
        menuService.update(Objects.requireNonNull(conversionService.convert(menuDto, Menu.class)));
        return Mono.empty();
    }

    @Operation(summary = "Удалить меню", description = "Удалить меню по id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Успшено удален"),
            @ApiResponse(responseCode = "404", description = "Меню с отправленным id не было найдено",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
                }
            )
        })
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(
        @Parameter(description = "ID удаляемого меню", example = "1", required = true)
        @RequestParam(name="id") Long menuId
    ) {
        menuService.delete(menuId);
        return Mono.empty();
    }

    @Operation(summary = "Найти меню", description = "При указании id ищет продукт по id, иначе возвращает список продуктов по указанной странице")
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200", 
                description = "Если было указано id, тело содержит соответствующее меню, иначе список из меню по указанной странице",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = MenuDto.class)),
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MenuDto.class)))
                }
            ),
            @ApiResponse(responseCode = "404", description = "Меню с указанным ID не было найдено",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
                }
            )
        })
    @GetMapping
    public Mono<ResponseEntity<String>> find(
        @Parameter(description = "ID продукта", example = "1")
        @RequestParam(required=false) Long id,
        @Parameter(description = "Имя пользователя", example = "Olya")
        @RequestParam(required=false) String username,
        @Parameter(description = "Номер страницы (нумерация с 0)", example = "0")
        @RequestParam(name="pnumber", required=false) Integer _pageNumber,
        @Parameter(description = "Размер страницы (по умолчанию 50)", example = "10")
        @RequestParam(name="psize", required=false) Integer _pageSize
    ) {
        if (id != null) {
            return findById(id);
        } else if (username != null) {
            return findAllByUsername(username);
        } else {
            Integer pageNumber = (_pageNumber == null) ? 0 : _pageNumber;
            Integer pageSize = (_pageSize == null) 
                ? pagingConf.getDefaultPageSize()
                : (_pageSize > pagingConf.getMaxPageSize())
                    ? pagingConf.getMaxPageSize()
                    : _pageSize;
            return findAll(pageNumber, pageSize);
        }
    }

    public Mono<ResponseEntity<String>> findAllByUsername(String username) {
        return menuService.findAllByUsername(username)
                .map((it) -> Objects.requireNonNull(conversionService.convert(it, MenuDto.class)))
                .collectList()
                .map(menusDto -> ResponseEntity.ok(jsonConvService.conv(menusDto)));
    }

    public Mono<ResponseEntity<String>> findAll(Integer pageNumber, Integer pageSize) {
        return menuService.findAll(pageNumber, pageSize)
        .map((it) -> Objects.requireNonNull(conversionService.convert(it, MenuDto.class)))
        .collectList()
        .map(menusDto -> ResponseEntity.ok(jsonConvService.conv(menusDto)));
    }

    public Mono<ResponseEntity<String>> findById(Long id) {
        return menuService.findById(id)
        .map(menu -> ResponseEntity.ok(jsonConvService.conv(
                conversionService.convert(menu, MenuDto.class)
            )))
        .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(summary = "Добавляет блюдо в меню, если оно еще не было в нем", description = "При наличии меню с указанным ip в базе, добавляет в него блюдо")
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "204", 
                description = "Блюдо успешно добавлено в меню"
            ),
            @ApiResponse(responseCode = "404", description = "Меню или блюдо с указанным ID не было найдено",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
                }
            )
        })
    @PutMapping("/dishes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> addDish(@RequestBody MenuDishDto dto) {
        menuService.includeDishToMenu(dto.dishId(), dto.menuId());
        return Mono.empty();
    }

    @Operation(summary = "Получить список блюд в составе меню", description = "При наличии меню с указанным ip в базе возвращает список блюд в нем")
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200", 
                description = "Тело содержит список блюд в меню с указанным id",
                content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DishDto.class)))
                }
            ),
            @ApiResponse(responseCode = "404", description = "Меню с указанным ID не было найдено",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
                }
            )
        })
    @GetMapping("/dishes")
    public Flux<DishDto> getDishes(
        @Parameter(description = "ID меню", example = "1", required = true)
        @RequestParam() Long id
    ) {
        return menuService.makeListOfDishes(id)
                .map((it) -> Objects.requireNonNull(conversionService.convert(it, DishDto.class)));
    }

    @Operation(summary = "Удалить блюдо из меню", description = "При наличии меню с указанным ip удаляет из него блюдо с указанным id")
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "204", 
                description = "Блюдо успешно удалено из меню"
            ),
            @ApiResponse(responseCode = "404", description = "Меню или блюдо с указанным ID не было найдено",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
                }
            )
        })
    @DeleteMapping("/dishes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteDish(@RequestBody MenuDishDto dto) {
        menuService.deleteDishFromMenu(dto.dishId(), dto.menuId());
        return Mono.empty();
    }
}
