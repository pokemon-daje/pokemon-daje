package com.pokemon.daje.model.api_dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PackageFrontEnd {
    @JsonProperty("exchange_id")
    private String exchangeId;
    @JsonProperty("status_response_code")
    private int status;
    @JsonProperty("status_request_code")
    private int requestStatus;
    @JsonProperty("pokemon_sent")
    private PokemonFrontEndDTO pokemonSent;
    @JsonProperty("pokemon_receive")
    private PokemonFrontEndDTO pokemonReceive;


    public PackageFrontEnd(String exchangeId, int status, int requestStatus,PokemonFrontEndDTO pokemonSent, PokemonFrontEndDTO pokemonReceive) {
        this.exchangeId = exchangeId;
        this.status = status;
        this.requestStatus = requestStatus;
        this.pokemonSent = pokemonSent;
        this.pokemonReceive = pokemonReceive;
    }
}
