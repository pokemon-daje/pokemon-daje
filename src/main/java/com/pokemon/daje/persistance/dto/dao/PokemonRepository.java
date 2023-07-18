package com.pokemon.daje.persistance.dto.dao;

import com.pokemon.daje.persistance.dto.PokemonDTO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PokemonRepository extends JpaRepository<PokemonDTO,Integer> {
}
