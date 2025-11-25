package org.itmo.secs.model.dto;

import java.util.List;
import org.springframework.data.util.Pair;

record DishDto(
    Long id,
    String name,
    List<Pair<ItemDto, Integer>> counts
) { }
