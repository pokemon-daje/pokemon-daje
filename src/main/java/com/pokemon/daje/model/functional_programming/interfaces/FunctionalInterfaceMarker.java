package com.pokemon.daje.model.functional_programming.interfaces;

import com.pokemon.daje.model.functional_programming.objects.ValueEnum;
import com.pokemon.daje.model.functional_programming.objects.WrapperValue;

import java.util.Map;
@FunctionalInterface
public interface FunctionalInterfaceMarker<A> {
    A doFunction(Map<ValueEnum, WrapperValue> values);
}
