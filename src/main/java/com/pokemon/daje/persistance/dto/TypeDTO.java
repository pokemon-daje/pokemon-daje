package com.pokemon.daje.persistance.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "type")
public class TypeDTO implements DTOInterface {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int dbId;
    @Column(name = "pokedex_type_id")
    private int pokedexId;
    @Column(name = "name")
    private String name;
    @Column(name = "image_url")
    private String imageUrl;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeDTO typeDTO = (TypeDTO) o;
        return pokedexId == typeDTO.pokedexId && Objects.equals(name, typeDTO.name) && Objects.equals(imageUrl, typeDTO.imageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pokedexId, name, imageUrl);
    }
}
