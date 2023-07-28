package com.pokemon.daje.model.functional_programming;

import com.pokemon.daje.controller.json.dto.PackageExchange;
import com.pokemon.daje.controller.json.dto.PokemonExchangeDTO;

import java.util.Map;

@FunctionalInterface
public interface FirstSwapFunction extends FunctionalInterfaceMarker<PackageExchange, PokemonExchangeDTO>{
    <D extends Object> PackageExchange doFunction(PokemonExchangeDTO parameter, Map<ValueEnum,D> values);
}

