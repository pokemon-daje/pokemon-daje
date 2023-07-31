package com.pokemon.daje.service;

import com.pokemon.daje.model.api_objects.ConcludeSwapRequest;
import com.pokemon.daje.model.api_objects.InitializeExchangeResponse;
import com.pokemon.daje.model.api_objects.PokemonRequestExchangeDTO;
import com.pokemon.daje.model.functional_programming.interfaces.FunctionalInterface;
import com.pokemon.daje.model.functional_programming.objects.*;
import com.pokemon.daje.model.business_data.PokemonSwapDeposit;
import com.pokemon.daje.model.business_data.ProgressingProcessCode;
import com.pokemon.daje.model.business_data.SwapBankAction;
import com.pokemon.daje.persistance.dto.PokemonDTO;
import com.pokemon.daje.util.SwapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class SwapScheduleService {
    private final Map<String, PokemonSwapDeposit> swapBank;
    private final Map<String, PokemonSwapDeposit> swapCacheLog;
    private final LinkedList<ExchangeRequestInteraction> queueOfRequests;
    private final List<PokemonDTO> randomPokemonStorage;
    private final Map<SwapFunctionAction, SwapExecutioner> executionersMap;
    private final ArrayBlockingQueue<ExecutorService> executionersThreads;
    private final SwapUtils swapUtils;

    @Autowired
    public SwapScheduleService(SwapUtils swapUtils) {
        this.swapUtils = swapUtils;
        this.randomPokemonStorage = new ArrayList<>();
        this.swapBank = new HashMap<>();
        this.swapCacheLog = new HashMap<>();
        this.executionersMap = new EnumMap<>(SwapFunctionAction.class);
        this.queueOfRequests = new LinkedList<>();
        this.executionersThreads = new ArrayBlockingQueue<>(2);
        loadInizializeSwapExecutionInSwapBank();
        loadConcludeSwapExecutionInSwapBank();
        swapUtils.loadPokemonsFromDatabase(this);
    }

    public <A> void addExecutioner(SwapFunctionAction functionAction, SwapExecutioner observer) {
        executionersMap.put(functionAction, observer);
    }

    public void addDeposit(String exchangeId, PokemonDTO pokemonToSave, PokemonDTO pokemonDTOToGive) {
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

    public boolean doDepositExist(String exchangeId) {
        return swapBank.get(exchangeId) != null;
    }

    public PokemonSwapDeposit getDeposit(String exchangeId) {
        return swapBank.get(exchangeId);
    }

    public PokemonSwapDeposit getCacheOfDeposit(String exchangeId) {
        return swapCacheLog.get(exchangeId);
    }

    public void removeDeposit(String exchangeId) {
        swapBank.remove(exchangeId);
    }

    public boolean isPokemonStorageEmpty() {
        return randomPokemonStorage.isEmpty();
    }

    public int pokemonStorageSize() {
        return randomPokemonStorage.size();
    }

    public PokemonDTO removeAndReturnPokemonByPosition(int position) {
        return randomPokemonStorage.remove(position);
    }

    public void removePokemonFromStorage(PokemonDTO pokemonDTO) {
        randomPokemonStorage.remove(pokemonDTO);
    }

    public void addPokemonToStorage(PokemonDTO pokemonDTO) {
        randomPokemonStorage.add(pokemonDTO);
    }

    public List<PokemonDTO> getPokemonStorage() {
        return new ArrayList<>(this.randomPokemonStorage);
    }

    public void addQueueRequest(ExchangeRequestInteraction exchangeRequestInteraction) {
        queueOfRequests.add(exchangeRequestInteraction);
    }

    private synchronized ExchangeRequestInteraction removeFirst(){
        ExchangeRequestInteraction waiter = null;
        if(!queueOfRequests.isEmpty()){
            waiter = queueOfRequests.removeFirst();
        }
        return waiter;
    }
    private <D extends Object> void responseInizializeExchange(ExchangeRequestInteraction waiter, D responseObject) {
        int code = HttpStatus.OK.value();
        if (responseObject == null) {
            code = HttpStatus.BAD_REQUEST.value();
        }
        waiter.sendData(responseObject, code);
    }

    private <D extends Object> void responseConcludeExchange(ExchangeRequestInteraction waiter, D responseObject) {
        int code = 0;
        if (responseObject != null && responseObject.getClass().equals(ProgressingProcessCode.class)) {
            ProgressingProcessCode progressCode = (ProgressingProcessCode) responseObject;
            switch (progressCode) {
                case POKEMON_REQUEST_SUCCESS -> code = HttpStatus.OK.value();
                case POKEMON_BAD_REQUEST -> code = HttpStatus.BAD_REQUEST.value();

                case POKEMON_EXCHANGE_NOT_FOUND -> code = HttpStatus.NOT_FOUND.value();

                default -> code = HttpStatus.INTERNAL_SERVER_ERROR.value();
            }
        }
        waiter.sendData(null, code);
    }

    private <D extends Object> void manageResponse(ExchangeRequestInteraction waiter, D responseObject) {
        if (responseObject != null && InitializeExchangeResponse.class.equals(responseObject.getClass())) {
            responseInizializeExchange(waiter, responseObject);
        } else if (responseObject != null && ProgressingProcessCode.class.equals(responseObject.getClass())) {
            responseConcludeExchange(waiter, responseObject);
        } else {
            waiter.sendData(null, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private void loadInizializeSwapExecutionInSwapBank() {
        FunctionalInterface function = valuesMap -> {
            ValueWrapper valueWithPokemon = valuesMap.get(ValueEnum.POKEMON_RECEIVED_TO_EXCHANGE);
            PokemonRequestExchangeDTO pokemon = null;
            if (valueWithPokemon != null) {
                pokemon = (PokemonRequestExchangeDTO) valueWithPokemon.getValue();
            }
            return swapUtils.inizializePokemonsSwap(this, pokemon);
        };
        SwapExecutioner inizializeSwapExecution = new SwapExecutioner(function);
        addExecutioner(SwapFunctionAction.INIZIALIZE_SWAP, inizializeSwapExecution);
    }

    private void loadConcludeSwapExecutionInSwapBank() {
        FunctionalInterface function = valuesMap -> {
            ConcludeSwapRequest packageStatus = (ConcludeSwapRequest) valuesMap.get(ValueEnum.POKEMON_REQUEST_SWAP_DTO).getValue();
            String exchangeId = (String) valuesMap.get(ValueEnum.EXCHANGE_ID).getValue();

            return swapUtils.concludeSwap(this, exchangeId, packageStatus);
        };
        SwapExecutioner concludeFunction = new SwapExecutioner(function);
        addExecutioner(SwapFunctionAction.CONCLUDE_SWAP, concludeFunction);
    }

    @Scheduled(fixedDelay = 1000)
    private void nextWaiter() {
        if (executionersThreads.remainingCapacity() > 0 && !isPokemonStorageEmpty() && !queueOfRequests.isEmpty()) {
            for (int i = 0; i < executionersThreads.remainingCapacity(); i++) {
                ExecutorService swapExecutor = Executors.newSingleThreadExecutor();
                if (!queueOfRequests.isEmpty() && !isPokemonStorageEmpty()) {
                    executionersThreads.add(swapExecutor);
                    swapExecutor.execute(() -> {
                        ExchangeRequestInteraction waiter = removeFirst();
                        if (waiter != null) {
                            SwapExecutioner swapExecutioner = executionersMap.get(waiter.getAction());
                            if (swapExecutioner != null) {
                                try {
                                    manageResponse(waiter, swapExecutioner.doFunction(waiter.getAllValues()));
                                } catch (Exception ex) {
                                    manageResponse(waiter, null);
                                    executionersThreads.remove(swapExecutor);
                                }
                            }
                        }
                        executionersThreads.remove(swapExecutor);
                    });
                }
            }
        }
    }

    @Scheduled(fixedDelay = 10000)
    private void checkTimeBank() {
        List<String> spoiledExchange = new ArrayList<>();
        swapBank.forEach((key, exchange) -> {
            if (System.currentTimeMillis() - exchange.getDepositTime() >= 5000) {
                spoiledExchange.add(key);
            }
        });
        spoiledExchange.forEach(key -> {
            PokemonSwapDeposit deposit = swapBank.get(key);
            if (randomPokemonStorage.size() < 6) {
                randomPokemonStorage.add(deposit.getPokemonToDelete());
            }
            swapBank.remove(key);
        });
    }

    @Scheduled(fixedDelay = 90000)
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
