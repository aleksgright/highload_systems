package org.itmo.secs.services;

import org.itmo.secs.model.entities.Menu;
import org.itmo.secs.repositories.MenuRepository;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MenuService {
    private MenuRepository menuRep;

    public void save(Menu menu) {
        menuRep.save(menu);
    }
}
