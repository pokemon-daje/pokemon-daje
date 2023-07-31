package com.pokemon.daje.model.functional_programming.objects;

import com.pokemon.daje.model.functional_programming.interfaces.FunctionalInterface;

import java.util.Map;

public class SwapExecutioner implements FunctionalInterface {
    FunctionalInterface function;
    public SwapExecutioner(FunctionalInterface function){
        this.function = function;
    }
    public Object doFunction(Map<ValueEnum, ValueWrapper> values){
        return function.doFunction(values);
    }
}
