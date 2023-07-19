package com.pokemon.daje.persistance.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
}
