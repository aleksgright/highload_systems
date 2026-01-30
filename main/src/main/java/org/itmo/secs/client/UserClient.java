package org.itmo.secs.client;

import org.itmo.secs.model.dto.UserDto;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

@Component
@ReactiveFeignClient(name = "user-service")
@ResponseBody
public interface UserClient {
    @GetMapping("/user")
    Mono<UserDto> getUserById(@PathVariable("id") Long id);
    @GetMapping("/user")
    Mono<UserDto> getUserByName(@PathVariable("name") String username);
}
