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

    public Item findById(long id) {
        return itemRepository.findById(id).orElse(null);
    }

    public Item getItemByName(String name)
    {
        return itemRepository.findByName(name).orElse(null);
    }
}
