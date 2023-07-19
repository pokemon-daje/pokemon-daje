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
public class Type implements BusinessInterface{

    private Integer id;
    private String name;
    private String imageUrl;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Type type = (Type) o;
        return id.equals(type.id) && name.equals(type.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}