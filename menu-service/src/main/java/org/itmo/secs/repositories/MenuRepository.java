package org.itmo.secs.repositories;

import org.itmo.secs.model.entities.Menu;
import org.itmo.secs.model.entities.enums.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends ReactiveCrudRepository<Menu, Long> {
    Mono<Menu> findByMealAndDateAndUserId(Meal meal, LocalDate date, Long userId);
    Flux<Menu> findAllByDate(LocalDate date);
    Flux<Menu> findAllByUserId(Long userId);
}
