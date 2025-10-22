package org.itmo.secs.controllers;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.model.dto.ItemDto;
import org.itmo.secs.services.ItemService;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping(value = "item")
public class ItemController {
    private final ConversionService conversionService;
    private final ItemService itemService;

    @PostMapping("/create")
    public ResponseEntity<Void> create(@RequestBody ItemDto itemDto)
    {
        itemService.saveItem(conversionService.convert(itemDto, Item.class));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/findByName")
    public ResponseEntity<Item> findByName(String name)
    {
        Item item = itemService.getItemByName(name);
        return item != null ? new ResponseEntity<>(item, HttpStatus.OK) : new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }
}