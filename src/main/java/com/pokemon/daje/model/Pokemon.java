package com.pokemon.daje.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;
@Getter
@Setter
public class Pokemon implements BusinessInterface {

    private Integer id;
    private String name;
    private String spriteUrl;
    private Integer currentHP;
    private Integer maxHP;
    private Type type;
    private Set<Move> moves;
    private String originalTrainer;
}