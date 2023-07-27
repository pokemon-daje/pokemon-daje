package com.pokemon.daje.controller.json.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pokemon.daje.persistance.dto.DTOInterface;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
@Getter
@Setter
public class PokemonExchangeDTO implements DTOInterface {
    @NotNull
    private Integer id;
    @NotNull
    private String name;
    @NotNull
    @JsonProperty("current_hp")
    private Integer currentHP;
    @NotNull
    @JsonProperty("max_hp")
    private Integer maxHP;
    @NotNull
    private Integer type;
    @NotNull
    @Size(min = 1,max = 4)
    private Set<Integer> moves;
    @NotNull
    @JsonProperty("original_trainer")
    private String originalTrainer;

    public PokemonExchangeDTO(){
        moves = new HashSet<>();
    }
}
