package com.pokemon.daje.controller.json.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pokemon.daje.persistance.dto.DTOInterface;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
@Getter
@Setter
public class PokemonExchangeDTO implements DTOInterface {
    private int id;
    private String name;
    @JsonProperty("current_hp")
    private int currentHP;
    @JsonProperty("max_hp")
    private int maxHP;
    private int type;
    private Set<Integer> moves;
    @JsonProperty("original_trainer")
    private String originalTrainer;

    public PokemonExchangeDTO(){
        moves = new HashSet<>();
    }
}
