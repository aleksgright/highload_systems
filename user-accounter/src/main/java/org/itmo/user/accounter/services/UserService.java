package org.itmo.user.accounter.services;

import org.itmo.user.accounter.utils.exceptions.DataIntegrityViolationException;
import org.itmo.user.accounter.utils.exceptions.ItemNotFoundException;
import org.itmo.user.accounter.repositories.UserRepository;
import org.itmo.user.accounter.model.entities.User;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class UserService {
    private UserRepository userRep;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Mono<User> save(User user) {
        return Mono.fromCallable(() -> userRep.findByName(user.getName()))
                .flatMap(optionalUser -> {
                    if (optionalUser.isPresent()) {
                        return Mono.error(new DataIntegrityViolationException("User with name " + user.getName() + " already exists"));
                    }
                    return Mono.fromCallable(() -> userRep.save(user));
                });
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Mono<User> update(User user) {
        return findById(user.getId())
                .switchIfEmpty(Mono.error(new ItemNotFoundException("User with id " + user.getId() + " was not found")))
                .flatMap(existingUser -> {
                    existingUser.setName(user.getName());
                    return Mono.fromCallable(() -> userRep.save(existingUser));
                });
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Mono<Void> deleteById(Long id) {
        return findById(id)
                .switchIfEmpty(Mono.error(new ItemNotFoundException("User with id " + id + " was not found")))
                .flatMap(user -> Mono.fromRunnable(() -> userRep.deleteById(id)));
    }

    public Mono<User> findByName(String name) {
        return Mono.fromCallable(() -> userRep.findByName(name))
                .flatMap(optionalUser -> optionalUser.map(Mono::just).orElse(Mono.empty()));
    }

    public Mono<User> findById(Long id) {
        return Mono.fromCallable(() -> userRep.findById(id))
                .flatMap(optionalUser -> optionalUser.map(Mono::just).orElse(Mono.empty()));
    }
}