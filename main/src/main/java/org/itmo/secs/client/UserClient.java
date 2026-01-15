package org.itmo.secs.client;

import org.itmo.secs.model.dto.UserDto;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactivefeign.spring.config.ReactiveFeignClient;

@Component
@ReactiveFeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/v1/user/{id}")
    UserDto getUserById(@PathVariable("id") Long id);
}