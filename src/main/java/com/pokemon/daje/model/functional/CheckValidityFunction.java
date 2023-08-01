package com.pokemon.daje.model.functional;

import com.pokemon.daje.model.ProgressingProcessCode;
import com.pokemon.daje.model.api_dto.PokemonExchangeDTO;

@FunctionalInterface
public interface CheckValidityFunction {
    ProgressingProcessCode checkValidity(PokemonExchangeDTO exchangeDTO);
}
