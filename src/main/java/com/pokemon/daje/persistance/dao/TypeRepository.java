package com.pokemon.daje.persistance.dao;

import com.pokemon.daje.persistance.dto.MoveDTO;
import com.pokemon.daje.persistance.dto.TypeDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TypeRepository extends JpaRepository<TypeDTO,Integer> {
    Optional<TypeDTO> findByPokedexId(int id);
    @Query("select t from TypeDTO as t where t.pokedexId = :id or t.pokedexId = 30000 ORDER BY(t.pokedexId) limit 1")
    Optional<TypeDTO> findByPokedexIdOrGetUnknow(@Param("id") int id);
}
