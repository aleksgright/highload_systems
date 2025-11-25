package org.itmo.secs.services;

import org.itmo.secs.repositories.UserRepository;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    private UserRepository userRep;

    public void save(User user) {
        userRep.save(user);
    }

    public void update(User user) {
        userRep.save(user);
    }

    public User findByName(String name) {
        var user = userRep.findByName(name);

        if (user.isEmpty()) {
            return null;
        } else {
            return user.get();
        }
    }
}
