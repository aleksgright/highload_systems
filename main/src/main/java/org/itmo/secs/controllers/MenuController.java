package org.itmo.secs.controllers;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.ArrayList;
import java.util.List;

import org.itmo.secs.model.dto.*;
import org.itmo.secs.model.entities.Dish;
import org.itmo.secs.model.entities.Menu;
import org.itmo.secs.services.*;
import org.itmo.secs.utils.conf.PagingConf;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping(value = "menu")
@Tag(name = "Меню (Menus API)")
public class MenuController {
    private MenuService menuService;
    private UserService userService;
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
    public ResponseEntity<MenuDto> create(@RequestBody MenuCreateDto menuDto) {
        Menu menu = conversionService.convert(menuDto, Menu.class);
        return new ResponseEntity<>(
                conversionService.convert(
                        menuService.save(menu),
                        MenuDto.class),
                HttpStatus.CREATED);
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
    public ResponseEntity<MenuDto> update(@RequestBody MenuDto menuDto) {
        Menu menu = conversionService.convert(menuDto, Menu.class);
        menuService.update(menu);
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<Void> delete(
        @Parameter(description = "ID удаляемого меню", example = "1", required = true)
        @RequestParam(name="id", required=true) Long menuId
    ) {
        menuService.delete(menuId);
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<String> find(
        @Parameter(description = "ID продукта", example = "1", required = false)
        @RequestParam(required=false) Long id,
        @Parameter(description = "Номер страницы (нумерация с 0)", example = "0", required = false)
        @RequestParam(name="pnumber", required=false) Integer _pageNumber,
        @Parameter(description = "Размер страницы (по умолчанию 50)", example = "10", required = false)
        @RequestParam(name="psize", required=false) Integer _pageSize
    ) {
        if (id != null) {
            return findById(id);
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

    public ResponseEntity<String> findAll(Integer pageNumber, Integer pageSize) {
        List<Menu> menus = menuService.findAll(pageNumber, pageSize);
        List<MenuDto> menusDto = new ArrayList<>();
        menus.forEach((Menu it) -> { 
            menusDto.add(conversionService.convert(it, MenuDto.class));
        });
        return ResponseEntity.ok(jsonConvService.conv(menusDto));
    }

    public ResponseEntity<String> findById(Long id) {
        Menu menu = menuService.findById(id);
        if (menu != null) {
            System.out.println(jsonConvService.conv(
                conversionService.convert(menu, MenuDto.class)
            ));
        }
        return (menu == null) 
            ? ResponseEntity.notFound().build()
            : ResponseEntity.ok(jsonConvService.conv(
                conversionService.convert(menu, MenuDto.class)
            ));
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
    public ResponseEntity<Void> addDish(@RequestBody MenuDishDto dto) {
        menuService.includeDishToMenu(dto.dishId(), dto.menuId());
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<String> getDishes(
        @Parameter(description = "ID меню", example = "1", required = true)
        @RequestParam(required=true) Long id
    ) {
        List<Dish> dishes = menuService.makeListOfDishes(id);

        List<DishDto> dishesDto = new ArrayList<>();

        dishes.forEach((Dish it) -> {
            dishesDto.add(conversionService.convert(it, DishDto.class));
        });

        return ResponseEntity.ok(jsonConvService.conv(dishesDto));
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
    public ResponseEntity<Void> deleteDish(@RequestBody MenuDishDto dto) {
        menuService.deleteDishFromMenu(dto.dishId(), dto.menuId());
        return ResponseEntity.noContent().build();
    }
}
