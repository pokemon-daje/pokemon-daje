package com.pokemon.daje.util;

import com.pokemon.daje.persistance.dao.PokemonRepository;
import com.pokemon.daje.persistance.dto.PokemonDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class RandomPokemonStorage {
    private final List<PokemonDTO> pokemonDTOList;

    @Autowired
    public RandomPokemonStorage(PokemonRepository pokemonRepository) {
        this.pokemonDTOList = pokemonRepository.getSixRandomPokemon();
    }

    public PokemonDTO swapPokemon(PokemonDTO pokemonDTO){
        Random random = new Random();
        PokemonDTO pokemonSwap = null;
        if (pokemonDTOList.size() > 0) {
            int randomPos = random.nextInt(0, pokemonDTOList.size());
            pokemonSwap = pokemonDTOList.remove(randomPos);
            pokemonDTOList.add(pokemonDTO);
        }
        return pokemonSwap;
    }
}
