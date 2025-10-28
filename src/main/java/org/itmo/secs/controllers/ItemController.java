package org.itmo.secs.controllers;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.dto.ItemUpdateDto;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.model.dto.ItemCreateDto;
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
    public ResponseEntity<Void> create(@RequestBody ItemCreateDto itemCreateDto)
    {
        itemService.save(conversionService.convert(itemCreateDto, Item.class));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> update(@RequestBody ItemUpdateDto itemUpdateDto)
    {
        itemService.update(conversionService.convert(itemUpdateDto, Item.class));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/findByName")
    public ResponseEntity<Item> findByName(String name)
    {
        Item item = itemService.findByName(name);
        return item != null ? new ResponseEntity<>(item, HttpStatus.OK) : new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }
}