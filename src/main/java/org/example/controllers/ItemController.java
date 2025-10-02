package org.example.controllers;

import lombok.AllArgsConstructor;
import org.example.entities.Item;
import org.example.repositories.ItemRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping(value = "item")
public class ItemController {
    private final ItemRepository itemRepository;

    @PostMapping("/add")
    public void addItem()
    {
        Item item = new Item();
        item.setId(1L);
        item.setName("Boris");
        item.setCalories(300);
        item.setFats(299);
        item.setCarbs(298);
        item.setProtein(287);
        itemRepository.save(item);
    }

    @GetMapping("/getByName")
    public String getItemByName(String name)
    {
        if (itemRepository.findByName(name).isEmpty()) return "Item not found";
        Item foundItem = itemRepository.findByName(name).get();
        return "Carbs: " + foundItem.getCarbs() + " fats: " + foundItem.getFats();
    }
}