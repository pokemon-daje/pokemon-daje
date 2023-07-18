package com.pokemon.daje.persistance.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "type")
public class TypeDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int dbId;
    @Column(name = "pokedex_type_id")
    private int id;
    @Column(name = "name")
    private String name;
    @Column(name = "image_url")
    private String imageUrl;
}
