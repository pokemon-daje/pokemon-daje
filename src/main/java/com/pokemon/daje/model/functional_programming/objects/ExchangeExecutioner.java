package com.pokemon.daje.model.functional_programming.objects;

import com.pokemon.daje.model.functional_programming.interfaces.FunctionalInterfaceMarker;

import java.util.Map;

public class ExchangeExecutioner<A extends Object> implements FunctionalInterfaceMarker<A> {
    FunctionalInterfaceMarker<A> function;
    public ExchangeExecutioner(FunctionalInterfaceMarker<A> function){
        this.function = function;
    }
    public A doFunction(Map<ValueEnum, WrapperValue> values){
        return function.doFunction(values);
    }
}
