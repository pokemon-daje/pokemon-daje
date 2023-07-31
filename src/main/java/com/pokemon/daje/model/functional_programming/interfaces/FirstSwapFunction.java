package com.pokemon.daje.model.functional_programming.interfaces;

import com.pokemon.daje.model.api_objects.PackageResponseExchange;
import com.pokemon.daje.model.functional_programming.objects.ValueEnum;
import com.pokemon.daje.model.functional_programming.objects.WrapperValue;

import java.util.Map;

@FunctionalInterface
public interface FirstSwapFunction extends FunctionalInterfaceMarker<PackageResponseExchange>{
    PackageResponseExchange doFunction(Map<ValueEnum, WrapperValue> values);
}

