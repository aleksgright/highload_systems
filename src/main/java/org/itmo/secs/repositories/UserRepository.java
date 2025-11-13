package org.itmo.secs.repositories;

import org.itmo.secs.model.entities.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository extends JpaRepository<User, Long> {
    public List<User> findAllByName(String name);
}
