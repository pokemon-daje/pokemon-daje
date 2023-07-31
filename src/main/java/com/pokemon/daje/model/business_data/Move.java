package com.pokemon.daje.model.business_data;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
@Getter
@Setter
public class Move implements BusinessInterface {

    private Integer id;
    private String name;
    private Type type;
    private Integer power;

    public Move() {
    }

    public Move(Integer id) {
        this.id = id;
    }

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

