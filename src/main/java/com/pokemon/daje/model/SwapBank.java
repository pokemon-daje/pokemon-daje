package com.pokemon.daje.model;

import com.pokemon.daje.controller.json.dto.WaiterExchangeResponse;
import com.pokemon.daje.persistance.dto.PokemonDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SwapBank {
    private Map<String, PokemonSwapDeposit> swapBank;
    private Map<String, PokemonSwapDeposit> swapCacheLog;
    private LinkedList<WaiterExchangeResponse> quequeOfRequests;
    private final List<PokemonDTO> randomPokemonStorage;

    public SwapBank() {
        this.swapBank = new HashMap<>();
        swapCacheLog = new HashMap<>();
        quequeOfRequests = new LinkedList<>();
        randomPokemonStorage = new ArrayList<>();
    }

    public void addDeposit(String exchangeId, PokemonDTO pokemonToSave,PokemonDTO pokemonDTOToGive){
        swapCacheLog.put(exchangeId,
                new PokemonSwapDeposit(
                        Map.of(
                                SwapBankAction.TOSAVE, pokemonToSave,
                                SwapBankAction.TODELETE, pokemonDTOToGive
                        )
                )
        );
        swapBank.put(exchangeId,
                new PokemonSwapDeposit(
                        Map.of(
                                SwapBankAction.TOSAVE, pokemonToSave,
                                SwapBankAction.TODELETE, pokemonDTOToGive
                        )
                )
        );
    }
    public boolean doDepositExist(String exchangeId){
        return swapBank.get(exchangeId)!=null;
    }
    public PokemonSwapDeposit getDeposit(String exchangeId){
        return swapBank.get(exchangeId);
    }
    public PokemonSwapDeposit getCacheOfDeposit(String exchangeId){
        return swapCacheLog.get(exchangeId);
    }

    public void removeDeposit(String exchangeId){
        swapBank.remove(exchangeId);
    }

    public boolean isPokemonStorageEmpty(){
        return randomPokemonStorage.isEmpty();
    }

    public int pokemonStorageSize(){
        return randomPokemonStorage.size();
    }

    public PokemonDTO removeAndReturnPokemonByPosition(int position){
        return randomPokemonStorage.remove(position);
    }
    public void removePokemonFromStorage(PokemonDTO pokemonDTO){
        randomPokemonStorage.remove(pokemonDTO);
    }

    public void addPokemonToStorage(PokemonDTO pokemonDTO){
        randomPokemonStorage.add(pokemonDTO);
    }
    public List<PokemonDTO> getPokemonStorage(){
        return new ArrayList<>(this.randomPokemonStorage);
    }

    @Scheduled(fixedDelay = 60000)
    private void checkTimeBank() {
        List<String> spoiledExchange = new ArrayList<>();
        swapBank.forEach((key, exchange) -> {
            if (System.currentTimeMillis() - exchange.getDepositTime() >= 5000) {
                spoiledExchange.add(key);
            }
        });
        spoiledExchange.forEach(key -> {
            PokemonSwapDeposit deposit = swapBank.get(key);
            if(randomPokemonStorage.size() < 6){
                randomPokemonStorage.add(deposit.getPokemonToDelete());
            }
            swapBank.remove(key);
        });
    }

    @Scheduled(fixedDelay = 30000)
    private void checkTimeBankChacheLog() {
        List<String> spoiledExchangeCache = new ArrayList<>();
        swapCacheLog.forEach((key, exchange) -> {
            if (System.currentTimeMillis() - exchange.getDepositTime() >= 30000) {
                spoiledExchangeCache.add(key);
            }
        });
        spoiledExchangeCache.forEach(key -> {
            swapCacheLog.remove(key);
        });
    }
}
