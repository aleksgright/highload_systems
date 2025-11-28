package org.itmo.secs.services;

import org.itmo.secs.utils.exceptions.*;
import org.itmo.secs.repositories.UserRepository;
import org.itmo.secs.utils.exceptions.ItemNotFoundException;
import org.itmo.secs.model.entities.User;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    private UserRepository userRep;

    public User save(User user) {
        if (findByName(user.getName()) != null) {
            throw new DataIntegrityViolationException("User with name " + user.getName() + " already exist");
        }
        return userRep.save(user);
    }

    public void update(User user) {
        User old = findById(user.getId());
        if (old == null) {
            throw new ItemNotFoundException("User with id " + user.getId() + " was not found");
        }
        old.setName(user.getName());
        userRep.save(old);
    }

    public User findByName(String name) {
        return userRep.findByName(name).orElse(null);
    }

    public User findById(Long id) {
        return userRep.findById(id).orElse(null);
    }
}
