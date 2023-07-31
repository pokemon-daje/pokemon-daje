package com.pokemon.daje.model.api_objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InitializeExchangeResponse {
    @JsonProperty("exchange_id")
    private String id;
    @JsonProperty("pokemon")
    private PokemonRequestExchangeDTO pokemonExchangeDTO;

    public InitializeExchangeResponse(String id, PokemonRequestExchangeDTO pokemonExchangeDTO) {
        this.id = id;
        this.pokemonExchangeDTO = pokemonExchangeDTO;
    }
}
