package com.pokemon.daje.controller.json.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GatherDataPokemonMove {
    private int pokedexID;
    private int power;
    private String type;
    private String name;
}
