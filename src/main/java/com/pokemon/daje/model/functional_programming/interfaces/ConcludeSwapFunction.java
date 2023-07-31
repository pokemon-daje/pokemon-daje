package com.pokemon.daje.model.functional_programming.interfaces;

import com.pokemon.daje.model.business_data.ProgressingProcessCode;
import com.pokemon.daje.model.functional_programming.objects.ValueEnum;
import com.pokemon.daje.model.functional_programming.objects.WrapperValue;

import java.util.Map;
@FunctionalInterface
public interface ConcludeSwapFunction extends FunctionalInterfaceMarker<ProgressingProcessCode>{
    ProgressingProcessCode doFunction(Map<ValueEnum, WrapperValue> values);
}
