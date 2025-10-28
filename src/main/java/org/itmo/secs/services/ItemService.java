package org.itmo.secs.services;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.repositories.ItemRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public void save(Item item)
    {
        itemRepository.save(item);
    }

    public void update(Item item)
    {
        itemRepository.save(item);
    }

    public Item findById(long id) {
        return itemRepository.findById(id).orElse(null);
    }

    public Item findByName(String name)
    {
        return itemRepository.findByName(name).orElse(null);
    }
}
