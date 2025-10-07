package org.itmo.secs.services;

import lombok.AllArgsConstructor;
import org.itmo.secs.entities.Item;
import org.itmo.secs.repositories.ItemRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public void saveItem()
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

    public String getItemByName(String name)
    {
        if (itemRepository.findByName(name).isEmpty()) return "Item not found";
        Item foundItem = itemRepository.findByName(name).get();
        return "Carbs: " + foundItem.getCarbs() + " fats: " + foundItem.getFats();
    }
}
