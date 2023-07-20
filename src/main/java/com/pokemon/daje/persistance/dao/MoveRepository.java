package com.pokemon.daje.persistance.dao;

import com.pokemon.daje.persistance.dto.MoveDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MoveRepository extends JpaRepository<MoveDTO,Integer> {
    Optional<MoveDTO> findByPokedexId(int id);
}
