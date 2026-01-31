package org.itmo.secs.model.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name = "menu_dishes")
@Getter
@Setter
@AllArgsConstructor
public class MenuDishes {
    @Id
    MenuDishesId id;
}

