package com.pokemon.daje.model.functional_programming;

import lombok.Getter;

@Getter
public class Value<A extends Object> {
    private final A value;
    public Value(A value){
        this.value = value;
    }
}
