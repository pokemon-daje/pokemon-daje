package com.pokemon.daje.persistance.dao;

import com.pokemon.daje.persistance.dto.MoveDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface MoveRepository extends JpaRepository<MoveDTO,Integer> {
    Optional<MoveDTO> findByPokedexId(int id);
    @Query("select m from MoveDTO as m where m.pokedexId = :id or m.pokedexId = 30000 ORDER BY(m.pokedexId) limit 1")
    Optional<MoveDTO> findByPokedexIdOrGetUnknow(@Param("id") int id);
    @Query(value = "SELECT * from move as m where m.id = any(select ms.move_db_id from pokemons_move_set as ms where ms.pokemon_db_id = :pokemonDBId)",nativeQuery = true)
    Set<MoveDTO> findAllByPokemonId(@Param("pokemonDBId") int pokemonDBId);
}
