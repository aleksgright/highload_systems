package org.itmo.secs.repositories;

import org.itmo.secs.model.entities.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDate;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {
    List<Meal> findAllByDate(LocalDate date);
}