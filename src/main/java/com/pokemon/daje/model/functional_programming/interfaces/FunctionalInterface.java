package com.pokemon.daje.model.functional_programming.interfaces;

import com.pokemon.daje.model.functional_programming.objects.ValueEnum;
import com.pokemon.daje.model.functional_programming.objects.ValueWrapper;

import java.util.Map;
@java.lang.FunctionalInterface
public interface FunctionalInterface {
    Object doFunction(Map<ValueEnum, ValueWrapper> values);
}
