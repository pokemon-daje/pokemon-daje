package com.pokemon.daje.controller.json.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PackageExchange {
    @JsonProperty("exchange_id")
    private String id;
    @JsonProperty("pokemon")
    private PokemonExchangeDTO pokemonExchangeDTO;

    public PackageExchange(String id, PokemonExchangeDTO pokemonExchangeDTO) {
        this.id = id;
        this.pokemonExchangeDTO = pokemonExchangeDTO;
    }
}
