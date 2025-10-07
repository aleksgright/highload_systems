package org.itmo.secs.controllers;

import lombok.AllArgsConstructor;
import org.itmo.secs.services.ItemService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping(value = "item")
public class ItemController {
    private final ItemService itemService;

    @PostMapping("/add")
    public void addItem()
    {
        itemService.saveItem();
    }

    @GetMapping("/getByName")
    public String getItemByName(String name)
    {
        return itemService.getItemByName(name);
    }
}