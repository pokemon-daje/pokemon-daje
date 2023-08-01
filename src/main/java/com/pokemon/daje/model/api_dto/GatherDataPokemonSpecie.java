package com.pokemon.daje.model.api_dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
@Getter
@Setter
public class GatherDataPokemonSpecie {
    private int id;
    private String name;
    @JsonProperty("current_hp")
    private int currentHP;
    @JsonProperty("max_hp")
    private int maxHP;
    private String type;
    private Set<Integer> moves;
    @JsonProperty("original_trainer")
    private String originalTrainer;
    private String sprite;
}
