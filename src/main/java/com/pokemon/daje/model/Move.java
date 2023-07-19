package com.pokemon.daje.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class Move implements BusinessInterface {

    private Integer id;
    private String name;
    private Type type;
    private Integer power;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return id.equals(move.id) && name.equals(move.name) && type.equals(move.type) && power.equals(move.power);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, power);
    }
}

