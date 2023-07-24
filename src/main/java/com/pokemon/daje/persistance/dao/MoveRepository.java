package com.pokemon.daje.persistance.dao;

import com.pokemon.daje.persistance.dto.MoveDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MoveRepository extends JpaRepository<MoveDTO,Integer> {
    Optional<MoveDTO> findByPokedexId(int id);
    @Query("select m from MoveDTO as m where m.pokedexId = :id or m.pokedexId = 30000 ORDER BY(m.pokedexId) limit 1")
    Optional<MoveDTO> findByPokedexIdOrGetUnknow(@Param("id") int id);
}
