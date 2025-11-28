package org.itmo.secs.services;

import java.time.LocalDate;
import java.time.ZoneId;

import org.itmo.secs.model.entities.Menu;
import org.itmo.secs.model.entities.User;
import org.itmo.secs.repositories.MenuRepository;
import org.itmo.secs.utils.exceptions.DataIntegrityViolationException;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MenuService {
    private MenuRepository menuRep;

    public Menu save(Menu menu) {
        if (
            findByKey(
                menu.getMeal().toString(), 
                LocalDate.now(ZoneId.of("Europe/Moscow")),
                menu.getUser().getId()
            ) != null
        ) {
            throw new DataIntegrityViolationException("Menu with given key already exist");
        }

        return menuRep.save(menu);
    }

    public void update() {
        var date = LocalDate.now(ZoneId.of("Europe/Moscow"));
    }

    public Menu findById(Long id) {
        return menuRep.findById(id).orElse(null);
    }

    public Menu findByKey(String meal, LocalDate date, long user_id) {
        return menuRep.findByMealAndDateAndUserId(meal, date, user_id).orElse(null);
    }
}
