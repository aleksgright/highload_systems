package org.itmo.user.accounter.services;

import org.itmo.user.accounter.utils.exceptions.DataIntegrityViolationException;
import org.itmo.user.accounter.utils.exceptions.ItemNotFoundException;
import org.itmo.user.accounter.repositories.UserRepository;
import org.itmo.user.accounter.model.entities.User;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void update(User user) {
        User old = findById(user.getId());
        if (old == null) {
            throw new ItemNotFoundException("User with id " + user.getId() + " was not found");
        }
        old.setName(user.getName());
        userRep.save(old);
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void deleteById(Long id) {
        User user = findById(id);
        if (user == null) {
            throw new ItemNotFoundException("User with id " + id.toString() + " was not found");
        }

        userRep.deleteById(id);
    }

    public User findByName(String name) {
        return userRep.findByName(name).orElse(null);
    }

    public User findById(Long id) {
        return userRep.findById(id).orElse(null);
    }
}