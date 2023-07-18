package com.pokemon.daje.persistance.dto.dao;

import com.pokemon.daje.persistance.dto.TypeDTO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeRepository extends JpaRepository<TypeDTO,Integer> {
}
