package com.pokemon.daje.controller.json.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pokemon.daje.model.Move;
import com.pokemon.daje.model.Type;
import com.pokemon.daje.persistance.dto.DTOInterface;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
@Getter
@Setter
public class PokemonFrontEndDTO implements DTOInterface {
    @JsonProperty("database_id")
    private int databaseId;
    @JsonProperty("pokedex_id")
    private Integer pokedexId;
    private String name;
    @JsonProperty("sprite_url")
    private String spriteUrl;
    @JsonProperty("current_hp")
    private Integer currentHP;
    @JsonProperty("max_hp")
    private Integer maxHP;
    private Type type;
    private Set<Move> moves;
    @JsonProperty("original_trainer")
    private String originalTrainer;

    public PokemonFrontEndDTO(){
        moves = new HashSet<>();
    }
}
