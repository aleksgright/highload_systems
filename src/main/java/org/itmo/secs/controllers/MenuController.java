package org.itmo.secs.controllers;

import org.springframework.web.bind.annotation.*;
import org.itmo.secs.model.dto.*;
import org.itmo.secs.model.entities.enums.*;
import org.itmo.secs.model.entities.Menu;
import org.itmo.secs.model.entities.User;
import org.itmo.secs.services.*;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping(value = "menu")
public class MenuController {
    private MenuService menuService;
    private UserService userService;
    private ConversionService conversionService;

    @PostMapping
    public ResponseEntity<MenuDto> create(@RequestBody MenuCreateDto menuDto) {
        Menu menu = conversionService.convert(menuDto, Menu.class);
        return new ResponseEntity<>(
                conversionService.convert(
                        menuService.save(menu),
                        MenuDto.class),
                HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<MenuDto> update(@RequestBody MenuUpdateDto menuDto) {
        Menu menu = new Menu();
        menu.setId(menuDto.id());
        menu.setMeal(Meal.valueOf(menuDto.meal()));
        menu.setUser(userService.findById(menuDto.userId()));
        return new ResponseEntity<>(
                conversionService.convert(
                        menuService.save(menu),
                        MenuDto.class),
                HttpStatus.CREATED);
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
