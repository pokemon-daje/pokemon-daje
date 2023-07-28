package com.pokemon.daje.model.functional_programming;

import com.pokemon.daje.controller.json.dto.PackageExchangeStatus;
import com.pokemon.daje.model.ProgressingProcessCode;

import java.util.Map;
@FunctionalInterface
public interface CompleteSwapFunction extends FunctionalInterfaceMarker<ProgressingProcessCode, PackageExchangeStatus>{
    <D extends Object> ProgressingProcessCode doFunction(PackageExchangeStatus parameter, Map<ValueEnum, Value<D>> values);
}
