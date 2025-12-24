package org.itmo.secs.controllers;

import lombok.AllArgsConstructor;

import java.util.List;
import org.springframework.data.util.Pair;
import java.util.ArrayList;

import org.itmo.secs.model.dto.*;
import org.itmo.secs.model.entities.Dish;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.services.*;
import org.itmo.secs.utils.conf.PagingConf;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@AllArgsConstructor
@RestController
@RequestMapping(value = "dish")
@Tag(name = "Блюда (Dishes API)")
public class DishController {
    private final DishService dishService;
    private final ItemDishService itemDishService;
    private final ConversionService conversionService;
    private final JsonConvService jsonConvService;
    private final PagingConf pagingConf;

    @Operation(summary = "Создать новое блюдо", description = "Создается новое блюдо по отправленному DTO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Блюдо было успешно создано",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DishDto.class))
                }
            ), 
            @ApiResponse(responseCode = "400", description = "Блюдо с таким же именем уже есть базе",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
                }
            )
        })
    @PostMapping
    public ResponseEntity<DishDto> create(@RequestBody DishCreateDto dishCreateDto) {
        try {
            return new ResponseEntity<>(
                    conversionService.convert(
                            dishService.save(conversionService.convert(dishCreateDto, Dish.class)),
                            DishDto.class),
                    HttpStatus.CREATED);
        } catch (ConversionException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Изменить блюдо", description = "Изменяет блюдо из БД по отправленному DTO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Успешно изменено"), 
            @ApiResponse(responseCode = "400", description = "Блюдо с именем из DTO уже существует",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
                }
            ),
            @ApiResponse(responseCode = "404", description = "Блюдо с id из DTO не было найдено",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
                }
            )
        })
    @PutMapping
    public ResponseEntity<Void> updateName(@RequestBody DishUpdateNameDto dishUpdateNameDto) {
        try {
            dishService.updateName(conversionService.convert(dishUpdateNameDto, Dish.class));
        } catch (ConversionException e) {
            return ResponseEntity.status(500).build();
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Найти блюда", description = "При указании id ищет блюдо по id, при неуказании id и указании имени ищет блюда по имени, иначе возвращает список блюд по указанной странице")
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200", 
                description = "Если были указаны id или имя, тело содержит соответствующее блюдо, иначе список блюд по указанной странице",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DishDto.class)),
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DishDto.class)))
                }
            ),
            @ApiResponse(responseCode = "404", description = "Блюдо с указанным именем или ID не было найдено",
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
        @RequestParam(name="psize", required=false) Integer _pageSize,
        @Parameter(description = "Имя продукта", example = "Творог", required = false)
        @RequestParam(required=false) String name
    ) {
        if (id != null) {
            return findById(id);
        } else if (name != null) {
            return findByName(name);
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

    public ResponseEntity<String> findById(Long id) {
        Dish dish = dishService.findById(id);
        return (dish != null)
        ? ResponseEntity.ok()
            .header("Content-Type", "application/json")
            .body(jsonConvService.conv(conversionService.convert(dish, DishDto.class)))
        : new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<String> findByName(String name) {
        Dish dish = dishService.findByName(name);
        return (dish != null)
        ? ResponseEntity.ok()
            .header("Content-Type", "application/json")
            .body(jsonConvService.conv(conversionService.convert(dish, DishDto.class)))
        : new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<String> findAll(Integer pageNumber, Integer pageSize) {
        List<Dish> dishes = dishService.findAll(pageNumber, pageSize);
        List<DishDto> dishesDto = new ArrayList<>();

        dishes.forEach(
            (Dish d) -> {
                dishesDto.add(conversionService.convert(d, DishDto.class));
            }
        );

        return ResponseEntity.ok().header("Content-Type", "application/json").body(jsonConvService.conv(dishesDto));
    }

    @Operation(summary = "Добавляет продукт в блюдо, если оно еще не было в нем", description = "При наличии блюда с указанным ip в базе, добавляет в него продукт")
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "204", 
                description = "Продукт успешно добавлен в меню"
            ),
            @ApiResponse(responseCode = "404", description = "Продукт или блюдо с указанным ID не было найдено",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
                }
            )
        })
    @PutMapping("/items")
    public ResponseEntity<Void> addItem(@RequestBody DishAddItemDto dishAddItemDto) {
        dishService.addItem(dishAddItemDto.itemId(), dishAddItemDto.dishId(), dishAddItemDto.count());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить блюдо", description = "Удалить блюдо по id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Успшено удалено"),
            @ApiResponse(responseCode = "404", description = "Блюдо с отправленным id не было найдено",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
                }
            )
        })
    @DeleteMapping
    public ResponseEntity<Void> delete(
        @Parameter(description = "ID удаляемого продукта", example = "1", required = true)
        @RequestParam(name="id", required=true) Long dishId
    ) {
        dishService.delete(dishId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить продукт из блюда", description = "При наличии блюда с указанным ip удаляет из него продукт с указанным id")
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "204", 
                description = "Продукт успешно удален из блюда"
            ),
            @ApiResponse(responseCode = "404", description = "Продукт или блюдо с указанным ID не было найдено",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
                }
            )
        })
    @DeleteMapping("/items")
    public ResponseEntity<Void> delete(
        @Parameter(description = "ID удаляемого продукта", example = "1", required = true)
        @RequestParam(name="item-id", required=true) Long itemId,
        @Parameter(description = "ID блюда", example = "2", required = true)
        @RequestParam(name="dish-id", required=true) Long dishId
    ) {
        dishService.deleteItem(itemId, dishId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить список продуктов в составе блюда", description = "При наличии блюда с указанным ip в базе возвращает список продуктов с граммовками")
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200", 
                description = "Тело содержит список продуктов с граммовками в составе блюда с указанным id",
                content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ItemCountDto.class)))
                }
            ),
            @ApiResponse(responseCode = "404", description = "Блюдо с указанным ID не было найдено",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
                }
            )
        })
    @GetMapping("/items")
    public ResponseEntity<String> getItems(
        @Parameter(description = "ID блюда", example = "1", required = true)
        @RequestParam(required=true) long id
    ) {
        List<Pair<Item, Integer>> items = dishService.makeListOfItems(id);

        List<ItemCountDto> itemsDto = new ArrayList<>();

        items.forEach((Pair<Item, Integer> it) -> {
            itemsDto.add(new ItemCountDto(conversionService.convert(it.getFirst(), ItemDto.class), it.getSecond()));
        });

        return ResponseEntity.ok().header("Content-Type", "application/json").body(jsonConvService.conv(itemsDto));
    }
}
