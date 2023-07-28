package com.pokemon.daje.model.functional_programming;

import java.util.Map;

public interface FunctionalInterfaceMarker<A,B> {
    <D extends Object> A doFunction(B parameter, Map<ValueEnum, Value<D>> values);
}
