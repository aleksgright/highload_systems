package org.itmo.secs.client;

import org.itmo.secs.model.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

@Component
@ReactiveFeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/user")
    Mono<ResponseEntity<UserDto>> getUserById(@PathVariable("id") Long id);
}