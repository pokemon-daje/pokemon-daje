package com.pokemon.daje.persistance.dao;

import com.pokemon.daje.persistance.dto.PokemonDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PokemonRepository extends JpaRepository<PokemonDTO,Integer> {
    @Query(value = "SELECT * FROM pokemons ORDER BY RAND() LIMIT 0,6;", nativeQuery = true)
    List<PokemonDTO> getSixRandomPokemon();
}
