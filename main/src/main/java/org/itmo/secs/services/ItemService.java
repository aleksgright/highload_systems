package org.itmo.secs.services;

import lombok.AllArgsConstructor;

import java.util.List;

import org.itmo.secs.model.entities.Item;
import org.itmo.secs.repositories.ItemRepository;
import org.itmo.secs.utils.exceptions.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@Service
@AllArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public Mono<Item> save(Item item) {
        if (findByName(item.getName()) != null) {
            throw new DataIntegrityViolationException("Item with name " + item.getName() + " already exist");
        }
        
        return Mono.just(itemRepository.save(item));
    }
    
    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void update(Item item) {
        findById(item.getId())
            .switchIfEmpty(Mono.error(new ItemNotFoundException("Item with id " + item.getId() + " was not found"))) 
            .map(x -> itemRepository.save(item)).subscribe();
    }

    public Mono<Item> findById(Long id) {
        Item item = itemRepository.findById(id).orElse(null);
        return (item != null) ? Mono.just(item) : Mono.empty();
    }

    public Mono<Item> findByName(String name) {
        Item item = itemRepository.findByName(name).orElse(null);
        return (item != null) ? Mono.just(item) : Mono.empty();
    }

    public Flux<Item> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return Flux.fromIterable(itemRepository.findAll(pageable).toList());
    }

    public Mono<Long> count() {
        return Mono.just(itemRepository.count());
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void delete(Long id) {
        if (itemRepository.findById(id).isEmpty()) {
            throw new ItemNotFoundException("Item with id " + id.toString() + " was not found");
        }
        itemRepository.deleteById(id);
    }
}
