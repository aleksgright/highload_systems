package org.itmo.secs.services;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.repositories.ItemRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public void saveItem(Item item)
    {
        itemRepository.save(item);
    }

    public String getItemByName(String name)
    {
        if (itemRepository.findByName(name).isEmpty()) return "Item not found";
        Item foundItem = itemRepository.findByName(name).get();
        return "Carbs: " + foundItem.getCarbs() + " fats: " + foundItem.getFats();
    }
}
