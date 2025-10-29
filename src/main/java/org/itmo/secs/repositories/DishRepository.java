package org.itmo.secs.repositories;

import org.itmo.secs.model.entities.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDate;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
    List<Dish> findAllByDate(LocalDate date);
}