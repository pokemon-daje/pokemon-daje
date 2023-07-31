package com.pokemon.daje.model.business_data;

import com.pokemon.daje.persistance.dto.PokemonDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
@Getter
@Setter
public class PokemonSwapDeposit {
    private Map<SwapBankAction,PokemonDTO> depositExchange;
    private long depositTime;

    public PokemonSwapDeposit(Map<SwapBankAction, PokemonDTO> depositExchange) {
        this.depositExchange = depositExchange;
        depositTime = System.currentTimeMillis();
    }

    public PokemonDTO getPokemonToSave(){
        return depositExchange.get(SwapBankAction.TOSAVE);
    }

    public PokemonDTO getPokemonToDelete(){
        return depositExchange.get(SwapBankAction.TODELETE);
    }
}
