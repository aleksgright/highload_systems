package org.itmo.secs.controllers;

import org.itmo.secs.model.dto.*;
import org.itmo.secs.model.entities.Item;
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

import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Users API")
public class UserController {
    private ConversionService conversionService;
    private UserService userService;

    @Operation(summary = "Create new user", description = "Create user by given name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created"), 
            @ApiResponse(responseCode = "400", description = "User with the same name already exists")
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

    @PutMapping
    public ResponseEntity<Void> update(@RequestBody UserDto userDto) {
        userService.update(new User(userDto.id(), userDto.name(), null));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam(required=true) long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<UserDto> find(
        @RequestParam(required=false) Long id,
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
