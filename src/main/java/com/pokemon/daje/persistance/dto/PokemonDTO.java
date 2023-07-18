package com.pokemon.daje.persistance.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
@Getter
@Setter
@Entity
@Table(name = "pokemons")
public class PokemonDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int dbId;
    @Column(name = "pokedex_id")
    private int id;
    @Column(name = "name")
    private String name;
    @Column(name = "sprite_url")
    private String spriteUrl;
    @Column(name = "current_health_points")
    private int currentHealthPoints;
    @Column(name = "max_health_points")
    private int maxHealthPoints;
    @ManyToMany
    @JoinTable(name="pokemons_move_set",
            joinColumns=
            @JoinColumn(name="pokemon_db_id", referencedColumnName="id"),
            inverseJoinColumns=
            @JoinColumn(name="move_db_id", referencedColumnName="id")
    )
    private Set<MoveDTO> moveSet;
    @Column(name = "trainer_name")
    private String trainerName;
}
