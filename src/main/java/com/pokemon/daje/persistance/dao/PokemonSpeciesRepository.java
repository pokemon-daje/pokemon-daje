package com.pokemon.daje.persistance.dao;

import com.pokemon.daje.persistance.dto.PokemonSpeciesDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PokemonSpeciesRepository extends JpaRepository<PokemonSpeciesDTO, Integer> {

    Optional<PokemonSpeciesDTO> findByPokedexId(Integer integer);
}
