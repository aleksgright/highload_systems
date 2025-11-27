package org.itmo.secs.controllers;

import org.itmo.secs.model.dto.UserCreateDto;
import org.itmo.secs.model.entities.User;
import org.itmo.secs.services.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping(value = "user")
public class UserController {
    private UserService userService;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody UserCreateDto userDto) {
        var user = new User();
        user.setName(userDto.name());
        return ResponseEntity.ok().build();
    }
}
