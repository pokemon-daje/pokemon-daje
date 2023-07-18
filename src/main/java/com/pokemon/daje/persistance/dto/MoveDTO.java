package com.pokemon.daje.persistance.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "move")
public class MoveDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int dbId;
    @Column(name = "pokedex_move_id")
    private int id;
    @Column(name = "name")
    private String name;
    @OneToOne
    @JoinColumn(name = "type_id", referencedColumnName = "dbId")
    private TypeDTO type;
    @Column(name = "power")
    private int power;
}
