package com.pokemon.daje.service;

import com.pokemon.daje.model.functional_programming.FunctionalInterfaceMarker;
import com.pokemon.daje.model.functional_programming.SwapFunctionAction;

import java.util.HashMap;
import java.util.Map;

public class ObserverSwapBank {
    Map<SwapFunctionAction, FunctionalInterfaceMarker> functions;
    ObserverSwapBank(){
        this.functions = new HashMap<>();
    }
    public <A extends Object,B extends Object> void addFunction(SwapFunctionAction action,FunctionalInterfaceMarker<A,B> function){
        functions.put(action,function);
    }
}
