package com.pokemon.daje.persistance.dao;

import com.pokemon.daje.persistance.dto.MoveDTO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoveRepository extends JpaRepository<MoveDTO,Integer> {
}
