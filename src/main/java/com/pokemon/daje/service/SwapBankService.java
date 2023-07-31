package com.pokemon.daje.service;

import com.pokemon.daje.model.api_objects.PackageExchangeStatus;
import com.pokemon.daje.model.api_objects.PackageResponseExchange;
import com.pokemon.daje.model.api_objects.PokemonRequestExchangeDTO;
import com.pokemon.daje.model.functional_programming.interfaces.ConcludeSwapFunction;
import com.pokemon.daje.model.functional_programming.interfaces.FirstSwapFunction;
import com.pokemon.daje.model.functional_programming.objects.*;
import com.pokemon.daje.model.business_data.PokemonSwapDeposit;
import com.pokemon.daje.model.business_data.ProgressingProcessCode;
import com.pokemon.daje.model.business_data.SwapBankAction;
import com.pokemon.daje.persistance.dto.PokemonDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class SwapBankService {
    private final Map<String, PokemonSwapDeposit> swapBank;
    private final Map<String, PokemonSwapDeposit> swapCacheLog;
    private final LinkedList<ExchangeRequestInteraction> quequeOfRequests;
    private final List<PokemonDTO> randomPokemonStorage;
    private final Map<SwapFunctionAction, ExchangeExecutioner> executionersMap;
    private final ArrayBlockingQueue<ExecutorService> executionersThreads;
    private final SwapBankLogistic swapBankLogistic;

    @Autowired
    public SwapBankService(SwapBankLogistic swapBankLogistic) {
        this.swapBankLogistic = swapBankLogistic;
        this.randomPokemonStorage = new ArrayList<>();
        this.swapBank = new HashMap<>();
        this.swapCacheLog = new HashMap<>();
        this.executionersMap = new HashMap<>();
        this.quequeOfRequests = new LinkedList<>();
        this.executionersThreads = new ArrayBlockingQueue<>(2);
        loadInizializeSwapExecutionInSwapBank();
        loadConcludeSwapExecutionInSwapBank();
        swapBankLogistic.loadPokemonsFromDatabase(this);
    }

    public void addExecutioner(SwapFunctionAction functionAction, ExchangeExecutioner observer) {
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
        quequeOfRequests.add(exchangeRequestInteraction);
    }

    private synchronized ExchangeRequestInteraction removeFirst(){
        ExchangeRequestInteraction waiter = null;
        if(!quequeOfRequests.isEmpty()){
            waiter = quequeOfRequests.removeFirst();
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
        int code = HttpStatus.INTERNAL_SERVER_ERROR.value();
        if (responseObject != null && responseObject.getClass().equals(ProgressingProcessCode.class)) {
            ProgressingProcessCode progerssCode = (ProgressingProcessCode) responseObject;
            switch (progerssCode) {
                case POKEMON_REQUEST_SUCCESS -> {
                    code = HttpStatus.OK.value();
                }
                case POKEMON_BAD_REQUEST -> {
                    code = HttpStatus.BAD_REQUEST.value();
                }
                case POKEMON_EXCHANGE_NOT_FOUND -> {
                    code = HttpStatus.NOT_FOUND.value();
                }
                default -> {
                    code = HttpStatus.INTERNAL_SERVER_ERROR.value();
                }
            }
        }
        waiter.sendData(null, code);
    }

    private <D extends Object> void manageResponse(ExchangeRequestInteraction waiter, D responseObject) {
        if (responseObject != null && PackageResponseExchange.class.equals(responseObject.getClass())) {
            responseInizializeExchange(waiter, responseObject);
        } else if (responseObject != null && ProgressingProcessCode.class.equals(responseObject.getClass())) {
            responseConcludeExchange(waiter, responseObject);
        } else {
            waiter.sendData(null, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private void loadInizializeSwapExecutionInSwapBank() {
        FirstSwapFunction function = valuesMap -> {
            WrapperValue valueWithPokemon = valuesMap.get(ValueEnum.POKEMON_RECEIVED_TO_EXCHANGE);
            PokemonRequestExchangeDTO pokemon = null;
            if (valueWithPokemon != null) {
                pokemon = (PokemonRequestExchangeDTO) valueWithPokemon.getValue();
            }
            return swapBankLogistic.inizializePokemonsSwap(this, pokemon);
        };
        ExchangeExecutioner<PackageResponseExchange> inizializeSwapExecution = new ExchangeExecutioner<>(function);
        addExecutioner(SwapFunctionAction.INIZIALIZE_SWAP, inizializeSwapExecution);
    }

    private void loadConcludeSwapExecutionInSwapBank() {
        ConcludeSwapFunction function = valuesMap -> {
            PackageExchangeStatus packageStatus = (PackageExchangeStatus) valuesMap.get(ValueEnum.PACKAGE_EXCHANGE_STATUS).getValue();
            String exchangeId = (String) valuesMap.get(ValueEnum.EXCHANGE_ID).getValue();

            return swapBankLogistic.concludeSwap(this, exchangeId, packageStatus);
        };
        ExchangeExecutioner<ProgressingProcessCode> concludeFunction = new ExchangeExecutioner<>(function);
        addExecutioner(SwapFunctionAction.CONCLUDE_SWAP, concludeFunction);
    }

    @Scheduled(fixedDelay = 1000)
    private void nextWaiter() {
        if (executionersThreads.remainingCapacity() > 0 && !isPokemonStorageEmpty() && !quequeOfRequests.isEmpty()) {
            for (int i = 0; i < executionersThreads.remainingCapacity(); i++) {
                if (!quequeOfRequests.isEmpty() && !isPokemonStorageEmpty()) {
                    ExecutorService swapExecutor = Executors.newSingleThreadExecutor();
                    executionersThreads.add(swapExecutor);
                    swapExecutor.execute(() -> {
                        ExchangeRequestInteraction waiter = removeFirst();
                        if (waiter != null) {
                            ExchangeExecutioner exchangeExecutioner = executionersMap.get(waiter.getAction());
                            if (exchangeExecutioner != null) {
                                try {
                                    manageResponse(waiter, exchangeExecutioner.doFunction(waiter.getAllValues()));
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
