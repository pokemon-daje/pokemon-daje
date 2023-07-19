package com.pokemon.daje.persistance.dao;

import com.pokemon.daje.persistance.dto.PokemonSpeciesDTO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PokemonSpeciesRepository extends JpaRepository<PokemonSpeciesDTO, Integer> {
}
