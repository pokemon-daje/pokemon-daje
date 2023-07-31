package com.pokemon.daje.model.functional_programming.objects;

public class ValueWrapper implements ValueInterface {
    private final Object value;
    public ValueWrapper(Object value){
        this.value = value;
    }
    @Override
    public Object getValue() {
        return value;
    }
}
