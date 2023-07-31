package com.pokemon.daje.model.functional_programming.objects;

public class WrapperValue<A extends Object> implements ValueInterface<A> {
    private final A value;
    public WrapperValue(A value){
        this.value = value;
    }
    @Override
    public A getValue() {
        return value;
    }
}
