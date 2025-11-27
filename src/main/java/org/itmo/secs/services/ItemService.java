package org.itmo.secs.services;

import lombok.AllArgsConstructor;

import java.util.List;

import org.itmo.secs.model.entities.Item;
import org.itmo.secs.repositories.ItemRepository;
import org.itmo.secs.utils.exceptions.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void save(Item item) {
        if (findByName(item.getName()) != null) {
            throw new DataIntegrityViolationException("Item with name " + item.getName() + " already exist");
        }
        
        itemRepository.save(item);
    }
    
    public void update(Item item) {
        itemRepository.save(item);
    }

    public Item findById(long id) {
        return itemRepository.findById(id).orElse(null);
    }

    public Item findByName(String name) {
        return itemRepository.findByName(name).orElse(null);
    }

    public List<Item> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return itemRepository.findAll(pageable).toList();
    }

    public long count() {
        return itemRepository.count();
    }
}
