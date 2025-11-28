package org.itmo.secs.controllers;

import org.itmo.secs.model.dto.*;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.model.entities.User;
import org.itmo.secs.services.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping(value = "user")
public class UserController {
    private ConversionService conversionService;
    private UserService userService;

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
