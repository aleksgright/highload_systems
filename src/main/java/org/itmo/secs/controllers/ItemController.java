package org.itmo.secs.controllers;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.model.dto.ItemDto;
import org.itmo.secs.services.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping(value = "item")
public class ItemController {
    private final ItemService itemService;

    @PostMapping("/add")
    public void addItem(@RequestBody ItemDto itemDto)
    {
        Item item = new Item(); //TODO add converter
        item.setName(itemDto.getName());
        item.setCalories(itemDto.getCalories());
        item.setCarbs(itemDto.getCarbs());
        item.setProtein(itemDto.getProtein());
        item.setFats(itemDto.getFats());
        itemService.saveItem(item);
    }

    @GetMapping("/getByName")
    public ResponseEntity<Item> getItemByName(String name)
    {
        Item item = itemService.getItemByName(name);
        return item != null ? new ResponseEntity<>(item, HttpStatus.OK) : new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }
}