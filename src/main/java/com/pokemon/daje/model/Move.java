package com.pokemon.daje.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class Move {

    private Integer id;
    private String name;
    private Type type;
    private Integer power;
}

