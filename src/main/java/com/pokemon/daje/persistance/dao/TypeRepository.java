package com.pokemon.daje.persistance.dao;

import com.pokemon.daje.persistance.dto.TypeDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TypeRepository extends JpaRepository<TypeDTO,Integer> {
    Optional<TypeDTO> findByPokedexId(int id);
}
