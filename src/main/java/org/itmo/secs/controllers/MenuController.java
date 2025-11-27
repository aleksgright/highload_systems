package org.itmo.secs.controllers;

import org.itmo.secs.model.dto.MenuAddDishDto;
import org.springframework.web.bind.annotation.*;
import org.itmo.secs.model.dto.*;
import org.itmo.secs.model.entities.User;
import org.itmo.secs.services.MenuService;
import org.springframework.http.ResponseEntity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping(value = "menu")
public class MenuController {
    private MenuService menuService;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody UserCreateDto userDto) {
        var user = new User();
        user.setName(userDto.name());
        return ResponseEntity.ok().build();
    }

    // @PutMapping("dishes")
    // public ResponseEntity<Void> addDish(@RequestBody MenuAddDishDto dto) {
        
    // }
}
