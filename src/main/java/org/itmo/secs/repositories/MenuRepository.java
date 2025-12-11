package org.itmo.secs.repositories;

import org.itmo.secs.model.entities.Menu;
import org.itmo.secs.model.entities.enums.Meal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByMealAndDateAndUserId(Meal meal, LocalDate date, Long user_id);
    List<Menu> findAllByDate(LocalDate date);
}
