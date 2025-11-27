package org.itmo.secs.controllers;

import lombok.AllArgsConstructor;

import org.itmo.secs.model.dto.ItemUpdateDto;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.model.dto.ItemCreateDto;
import org.itmo.secs.services.ItemService;
import org.itmo.secs.services.JsonConvService;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

import org.itmo.secs.model.dto.ItemDto;
import org.itmo.secs.utils.conf.PagingConf;

@AllArgsConstructor
@RestController
@RequestMapping(value = "item")
public class ItemController {
    private final ConversionService conversionService;
    private final ItemService itemService;
    private final JsonConvService jsonConvService;
    private final PagingConf pagingConf;

    @PostMapping
    public ResponseEntity<Item> create(@RequestBody ItemCreateDto itemCreateDto) {
        return new ResponseEntity<>(
                itemService.save(conversionService.convert(itemCreateDto, Item.class)),
                HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<Void> update(@RequestBody ItemUpdateDto itemUpdateDto) {
        itemService.update(conversionService.convert(itemUpdateDto, Item.class));
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<String> find(
        @RequestParam(required=false) Long id,
        @RequestParam(name="pnumber", required=false) Integer _pageNumber,
        @RequestParam(name="psize", required=false) Integer _pageSize,
        @RequestParam(required=false) String name
    ) {
        if (id != null && name == null && _pageNumber == null && _pageSize == null) {
            return findById(id);
        } else if (name != null && _pageNumber == null && _pageSize == null) {
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

    public ResponseEntity<String> findAll(Integer pageNumber, Integer pageSize) {
        List<Item> items = itemService.findAll(pageNumber, pageSize);
        List<ItemDto> itemsDto = new ArrayList<>();
        items.forEach((Item it) -> { 
            itemsDto.add(conversionService.convert(it, ItemDto.class));
        });
        return ResponseEntity
            .ok()
            .header("X-Total-Count: " + String.valueOf(itemService.count()))
            .body(jsonConvService.conv(itemsDto));
    }

    public ResponseEntity<String> findById(Long id) {
        Item item = itemService.findById(id);
        return (item == null) 
            ? ResponseEntity.status(HttpStatus.NOT_FOUND).build()
            : ResponseEntity.ok(jsonConvService.conv(
                conversionService.convert(item, ItemDto.class)
            ));
    }

    public ResponseEntity<String> findByName(String name) {
        Item item = itemService.findByName(name);
        return (item == null) 
            ? ResponseEntity.status(HttpStatus.NOT_FOUND).build()
            : ResponseEntity.ok(jsonConvService.conv(
                conversionService.convert(item, ItemDto.class)
            ));
    }
}