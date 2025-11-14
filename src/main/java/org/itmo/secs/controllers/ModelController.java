package org.itmo.secs.controllers;

import lombok.AllArgsConstructor;

import org.itmo.secs.model.entities.Item;
import org.itmo.secs.services.ItemService;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@Controller
@RequestMapping("/ui")
public class ModelController {
    private final ItemService itemService;
    private final ConversionService conversionService;

    @GetMapping("/item/show")
    public String showAll(Model model) {
        model.addAttribute("initialData", itemService.findAll(0));
        model.addAttribute("currentPage", 0);
        return "items";
    }

    @GetMapping("/item/creation")
    public String createItem(Model model) {
        return "create_item";
    }

    @GetMapping("/item/updating/{item_id}")
    public String createItem(@PathVariable("item_id") long id, Model model) {
        Item item = itemService.findById(id);
        if (item == null) {
            return "not_found";
        }

        model.addAttribute("item", item);

        return "update_item";
    }
}