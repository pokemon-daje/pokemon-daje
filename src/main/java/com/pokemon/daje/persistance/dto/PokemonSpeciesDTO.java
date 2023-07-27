package com.pokemon.daje.persistance.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pokemon_species")
public class PokemonSpeciesDTO implements DTOInterface {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int dbId;
    @Column(name = "pokedex_id")
    private int pokedexId;
    @Column(name = "name")
    private String name;
    @Column(name = "sprite_url")
    private String spriteUrl;
    @OneToOne
    @JoinColumn(name = "type_id", referencedColumnName = "pokedex_type_id")
    private TypeDTO type;
}
