package com.pokemon.daje.persistance.dao;

import com.pokemon.daje.persistance.dto.MoveDTO;
import com.pokemon.daje.persistance.dto.PokemonSpeciesDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PokemonSpeciesRepository extends JpaRepository<PokemonSpeciesDTO, Integer> {

    Optional<PokemonSpeciesDTO> findByPokedexId(Integer integer);
    @Query("select p from PokemonSpeciesDTO as p where p.pokedexId = :id or p.pokedexId = 30000 ORDER BY(p.pokedexId) limit 1")
    Optional<MoveDTO> findByPokedexIdOrGetUnknow(@Param("id") int id);
}
