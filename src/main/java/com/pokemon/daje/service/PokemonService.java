package com.pokemon.daje.service;

import com.pokemon.daje.util.marshaller.api.PokemonToFrontEndMarshaller;
import com.pokemon.daje.model.api_objects.ConcludeSwapRequest;
import com.pokemon.daje.model.api_objects.PokemonFrontEndDTO;
import com.pokemon.daje.model.api_objects.PokemonRequestExchangeDTO;
import com.pokemon.daje.model.business_data.Pokemon;
import com.pokemon.daje.model.business_data.PokemonSwapDeposit;
import com.pokemon.daje.model.business_data.ProgressingProcessCode;
import com.pokemon.daje.model.business_data.SwapBankAction;
import com.pokemon.daje.model.functional_programming.objects.*;
import com.pokemon.daje.persistance.dao.MoveRepository;
import com.pokemon.daje.persistance.dao.PokemonRepository;
import com.pokemon.daje.persistance.dao.PokemonSpeciesRepository;
import com.pokemon.daje.persistance.dto.MoveDTO;
import com.pokemon.daje.persistance.dto.PokemonDTO;
import com.pokemon.daje.persistance.dto.PokemonSpeciesDTO;
import com.pokemon.daje.util.marshaller.persistance.PokemonMarshaller;
import jakarta.servlet.AsyncContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

@Slf4j
@Service
public class PokemonService {
    private final PokemonRepository pokemonRepository;
    private final PokemonMarshaller pokemonMarshaller;
    private final PokemonToFrontEndMarshaller pokemonToFrontEndMarshaller;
    private final MoveRepository moveRepository;
    private final PokemonSpeciesRepository pokemonSpeciesRepository;
    private final SwapScheduleService swapScheduleService;
    private DataSource dataSource;
    @Autowired
    public PokemonService(PokemonRepository pokemonRepository,
                          PokemonMarshaller pokemonMarshaller,
                          PokemonToFrontEndMarshaller pokemonToFrontEndMarshaller,
                          MoveRepository moveRepository,
                          PokemonSpeciesRepository pokemonSpeciesRepository,
                          SwapScheduleService swapScheduleService) {
        this.pokemonRepository = pokemonRepository;
        this.pokemonMarshaller = pokemonMarshaller;
        this.pokemonToFrontEndMarshaller = pokemonToFrontEndMarshaller;
        this.moveRepository = moveRepository;
        this.pokemonSpeciesRepository = pokemonSpeciesRepository;
        this.swapScheduleService = swapScheduleService;
        this.dataSource = DataSourceBuilder.create()
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .url("jdbc:mysql://localhost:3306/daje")
                .username("daje")
                .password("daje")
                .build();
    }

    public PokemonFrontEndDTO getById(int pokemonId) {
        Pokemon pokemonBusiness = pokemonMarshaller.fromDTO(pokemonRepository.findById(pokemonId).orElse(null));
        return pokemonToFrontEndMarshaller.toDTO(pokemonBusiness);
    }
    public PokemonDTO insert(Pokemon pokemon) {
        PokemonDTO pokemonDTO = null;
        if (pokemon.getType() != null && pokemon.getId() != null) {
            pokemonDTO = pokemonMarshaller.toDTO(pokemon);
            normalizeDTO(pokemonDTO);
            pokemonDTO = pokemonDTO != null ? pokemonRepository.save(pokemonDTO):null;
        }
        return pokemonDTO;
    }
    public PokemonFrontEndDTO insertFromFrontEnd(Pokemon pokemon){
        PokemonFrontEndDTO pokemonFrontEnd = null;
        if (pokemon.getType() != null && pokemon.getId() != null) {
            PokemonDTO pokemonDTO = insert(pokemon);
            Pokemon pokemonBusinessFromDB = pokemonMarshaller.fromDTO(pokemonDTO);
            pokemonFrontEnd = pokemonToFrontEndMarshaller.toDTO(pokemonBusinessFromDB);
        }
        return pokemonFrontEnd;
    }
    public List<PokemonFrontEndDTO> getPokemonsFromStorage(){
        return swapScheduleService.getPokemonStorage().stream().map(pokemonDTO -> {
            int databaseId = pokemonDTO.getDbId();
            Optional<PokemonDTO> optionalPokemonDTO = pokemonRepository.findById(databaseId);
            if(optionalPokemonDTO.isPresent()){
                Pokemon businessPokemon = pokemonMarshaller.fromDTO(optionalPokemonDTO.get());
                PokemonFrontEndDTO pokemonFrontEndDTO = pokemonToFrontEndMarshaller.toDTO(businessPokemon);
                pokemonFrontEndDTO.setDatabaseId(databaseId);
                return pokemonFrontEndDTO;
            }
            return null;
        }).toList();
    }
    private void normalizeDTO(PokemonDTO pokemonToNormalize) {
        if(pokemonToNormalize != null){
            Set<MoveDTO> movesDTO = new HashSet<>();
            Optional<PokemonSpeciesDTO> speciesDTO = pokemonSpeciesRepository.findByPokedexIdOrGetUnknow(pokemonToNormalize.getPokemonSpeciesDTO().getPokedexId());
            pokemonToNormalize.getMoveSet().forEach(move -> {
                Optional<MoveDTO> moveDTO = moveRepository.findByPokedexIdOrGetUnknow(move.getPokedexId());
                moveDTO.ifPresent(movesDTO::add);
            });
            speciesDTO.ifPresent(pokemonToNormalize::setPokemonSpeciesDTO);
            pokemonToNormalize.setMoveSet(movesDTO);
        }
    }
    public Map<SwapBankAction,PokemonFrontEndDTO> getPokemonsFromSwapCacheLog(String exchangeId){
        PokemonSwapDeposit deposit = swapScheduleService.getCacheOfDeposit(exchangeId);
        PokemonFrontEndDTO toSave = null;
        PokemonFrontEndDTO toDelete = null;
        Map<SwapBankAction,PokemonFrontEndDTO> mapDeposit = new EnumMap<>(SwapBankAction.class);
        if(deposit!=null && deposit.getPokemonToDelete()!=null && deposit.getPokemonToSave()!=null){
            int databaseIdPokemonToDelete = deposit.getPokemonToDelete().getDbId();
            int databaseIdPokemonToSave = deposit.getPokemonToSave().getDbId();
            Pokemon pokemonToDelete = pokemonMarshaller.fromDTO(deposit.getPokemonToDelete());
            Pokemon pokemonToSave = pokemonMarshaller.fromDTO(deposit.getPokemonToSave());

            toSave = pokemonToFrontEndMarshaller.toDTO(pokemonToSave);
            toSave.setDatabaseId(databaseIdPokemonToSave);
            toDelete = pokemonToFrontEndMarshaller.toDTO(pokemonToDelete);
            toDelete.setDatabaseId(databaseIdPokemonToDelete);

            mapDeposit.put(SwapBankAction.TOSAVE,toSave);
            mapDeposit.put(SwapBankAction.TODELETE,toDelete);
        }
        return mapDeposit;
    }
    public void addInitializeSwapRequest(AsyncContext response, PokemonRequestExchangeDTO pokemonDTO){
        Map<ValueEnum, ValueWrapper> valuesMap = new EnumMap<>(ValueEnum.class);
        valuesMap.put(ValueEnum.REQUEST_CODE,new ValueWrapper(ProgressingProcessCode.POKEMON_EXCHANGE_REQUEST_OPEN));
        valuesMap.put(ValueEnum.POKEMON_RECEIVED_TO_EXCHANGE,new ValueWrapper(pokemonDTO));
        swapScheduleService.addQueueRequest(new ExchangeRequestInteraction(SwapFunctionAction.INIZIALIZE_SWAP,response,valuesMap));
    }
    public void addConcludeSwapRequest(AsyncContext response, String exchangeId, ConcludeSwapRequest concludeSwapRequest){
        Map<ValueEnum, ValueWrapper> valuesMap = new EnumMap<>(ValueEnum.class);
        ExchangeRequestInteraction waiter = new ExchangeRequestInteraction(SwapFunctionAction.CONCLUDE_SWAP,response);
        if(swapScheduleService.doDepositExist(exchangeId) && concludeSwapRequest != null && concludeSwapRequest.getStatus() != null){
            valuesMap.put(ValueEnum.EXCHANGE_ID, new ValueWrapper(exchangeId));
            valuesMap.put(ValueEnum.POKEMON_REQUEST_SWAP_DTO,new ValueWrapper(concludeSwapRequest));
            waiter.setValuesMap(valuesMap);
            swapScheduleService.addQueueRequest(waiter);
        }else{
            waiter.sendData(null, HttpStatus.NOT_FOUND.value());
        }
    }
    @Scheduled(fixedDelay = 2000)
    private void checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            log.info("database connesso e funzionante");
        } catch (Exception e) {
            log.info("database non connesso o non funzionante");
            System.exit(1);
        }
    }

}
