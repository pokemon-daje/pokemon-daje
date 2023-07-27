package com.pokemon.daje.persistance.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "move")
public class MoveDTO implements DTOInterface {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int dbId;
    @Column(name = "pokedex_move_id")
    private int pokedexId;
    @Column(name = "name")
    private String name;
    @OneToOne
    @JoinColumn(name = "type_id", referencedColumnName = "pokedex_type_id")
    private TypeDTO type;
    @Column(name = "power")
    private int power;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MoveDTO moveDTO = (MoveDTO) o;
        return pokedexId == moveDTO.pokedexId && power == moveDTO.power && Objects.equals(name, moveDTO.name) && Objects.equals(type, moveDTO.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pokedexId, name, type, power);
    }
}
