package org.itmo.secs.controllers;

import org.itmo.secs.model.dto.*;
import org.itmo.secs.model.entities.User;
import org.itmo.secs.services.UserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping(value = "user")
@Tag(name = "Пользователи (Users API)")
public class UserController {
    private ConversionService conversionService;
    private UserService userService;

    @Operation(summary = "Создать нового пользователя", description = "Создается новый пользователь по отправленному DTO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь был успешно создан",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))
                }
            ), 
            @ApiResponse(responseCode = "400", description = "Пользователь с таким же именем уже есть базе",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
                }
            )
        })
    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody UserCreateDto userDto) {
        User user = new User();
        user.setName(userDto.name());
        return new ResponseEntity<>(
                conversionService.convert(
                        userService.save(user),
                        UserDto.class),
                HttpStatus.CREATED);
    }

    @Operation(summary = "Изменить пользователя", description = "Изменяет пользователя из БД по отправленному DTO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Успешно изменен"), 
            @ApiResponse(responseCode = "400", description = "Пользователь с именем из DTO уже существует",
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
    public ResponseEntity<Void> update(@RequestBody UserDto userDto) {
        userService.update(new User(userDto.id(), userDto.name(), null));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить пользователя", description = "Удалить пользователя по id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Успшено удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь с отправленным id не был найден",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
                }
            )
        })
    @DeleteMapping
    public ResponseEntity<Void> delete(
        @Parameter(description = "ID удаляемого пользователя", example = "1", required = true)
        @RequestParam(required=true) long id
    ) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Найти пользователя", description = "При указании id ищет пользователя по id, при указании имени ищет пользователя по имени")
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200", 
                description = "Содержит найденного пользователя",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))
                }
            ),
            @ApiResponse(responseCode = "400", description = "Ни ID, ни имя для поиска не были указаны"),
            @ApiResponse(responseCode = "404", description = "Пользователь с указанным ID или именем не был найдено",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
                }
            )
        })
    @GetMapping
    public ResponseEntity<UserDto> find(
        @Parameter(description = "ID пользователя", example = "1", required = false)
        @RequestParam(required=false) Long id,
        @Parameter(description = "Имя пользователя", example = "Олежка", required = false)
        @RequestParam(required=false) String name
    ) {
        if (id != null) {
            User user = userService.findById(id);
            return (user != null)
            ? ResponseEntity.ok().body(conversionService.convert(user, UserDto.class))
            : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else if (name != null) {
            User user = userService.findByName(name);
            return (user != null)
            ? ResponseEntity.ok().body(conversionService.convert(user, UserDto.class))
            : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
