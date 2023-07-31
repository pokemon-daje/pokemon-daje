package com.pokemon.daje.util;

import com.pokemon.daje.model.api_objects.ConcludeSwapRequest;
import com.pokemon.daje.model.api_objects.InitializeExchangeResponse;
import com.pokemon.daje.model.api_objects.PokemonRequestExchangeDTO;
import com.pokemon.daje.model.business_data.Pokemon;
import com.pokemon.daje.model.business_data.PokemonSwapDeposit;
import com.pokemon.daje.model.business_data.ProgressingProcessCode;
import com.pokemon.daje.persistance.dao.MoveRepository;
import com.pokemon.daje.persistance.dao.PokemonRepository;
import com.pokemon.daje.persistance.dao.PokemonSpeciesRepository;
import com.pokemon.daje.persistance.dto.MoveDTO;
import com.pokemon.daje.persistance.dto.PokemonDTO;
import com.pokemon.daje.persistance.dto.PokemonSpeciesDTO;
import com.pokemon.daje.service.SwapScheduleService;
import com.pokemon.daje.util.exception.PokemonServiceException;
import com.pokemon.daje.util.marshaller.api.PokemonToExchangeMarshaller;
import com.pokemon.daje.util.marshaller.persistance.PokemonMarshaller;
import io.swagger.v3.core.util.ObjectMapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

@Slf4j
@Component
public class SwapUtils {
    private final PokemonRepository pokemonRepository;
    private final PokemonMarshaller pokemonMarshaller;
    private final PokemonToExchangeMarshaller pokemonToExchangeMarshaller;
    private final MoveRepository moveRepository;
    private final PokemonSpeciesRepository pokemonSpeciesRepository;
    @Value("${pokemon.fallback.path}")
    private String pathPokemonFallBack;

    public SwapUtils(PokemonRepository pokemonRepository,
                     PokemonMarshaller pokemonMarshaller,
                     PokemonToExchangeMarshaller pokemonToExchangeMarshaller,
                     MoveRepository moveRepository,
                     PokemonSpeciesRepository pokemonSpeciesRepository) {
        this.pokemonRepository = pokemonRepository;
        this.pokemonMarshaller = pokemonMarshaller;
        this.pokemonToExchangeMarshaller = pokemonToExchangeMarshaller;
        this.moveRepository = moveRepository;
        this.pokemonSpeciesRepository = pokemonSpeciesRepository;
    }

    public InitializeExchangeResponse inizializePokemonsSwap(SwapScheduleService swapScheduleService, PokemonRequestExchangeDTO pokemon) {
        InitializeExchangeResponse exchangeSwapDTO = null;
        if (pokemon != null) {
            PokemonDTO pokemonToPersistDTO = validateAndGivePokemonToSave(pokemon);
            int pokemonToGiveId = choosePokemonToSwap(swapScheduleService);
            Optional<PokemonDTO> pokemonDTOToGive= pokemonRepository.findById(pokemonToGiveId);
            if(pokemonDTOToGive.isPresent() && pokemonToPersistDTO != null){
                String idSwap = UUID.randomUUID().toString();
                swapScheduleService.addDeposit(idSwap,pokemonToPersistDTO,pokemonDTOToGive.get());
                Set<MoveDTO> moveDTOSet = moveRepository.findAllByPokemonId(pokemonToGiveId);
                pokemonDTOToGive.get().setMoveSet(moveDTOSet);
                exchangeSwapDTO = new InitializeExchangeResponse(idSwap, mapPokemonToGiveForExchange(pokemonDTOToGive.get()));
            }
        }
        return exchangeSwapDTO;
    }
    public ProgressingProcessCode concludeSwap(SwapScheduleService swapScheduleService, String exchangeid, ConcludeSwapRequest concludeSwapRequest) {
        ProgressingProcessCode code;
        if (concludeSwapRequest != null
                && concludeSwapRequest.getStatus() != null
                && concludeSwapRequest.getStatus() == ProgressingProcessCode.POKEMON_REQUEST_SUCCESS.getCode()
                && swapScheduleService.doDepositExist(exchangeid)) {
            PokemonSwapDeposit exchange = swapScheduleService.getDeposit(exchangeid);
            PokemonDTO pokemonToSave = exchange != null ? exchange.getPokemonToSave() : null;
            PokemonDTO pokemonToDelete = exchange != null ? exchange.getPokemonToDelete() : null;

            code = persistSwap(swapScheduleService,exchangeid,pokemonToSave, pokemonToDelete);
        }else if(concludeSwapRequest != null && concludeSwapRequest.getStatus() != null
                && !swapScheduleService.doDepositExist(exchangeid)){
            code = ProgressingProcessCode.POKEMON_EXCHANGE_NOT_FOUND;
        } else if (concludeSwapRequest != null && concludeSwapRequest.getStatus() != null
                && ProgressingProcessCode.fromNumber(concludeSwapRequest.getStatus()).equals(ProgressingProcessCode.POKEMON_REQUEST_UNKWON)) {
            swapScheduleService.removeDeposit(exchangeid);
            code = ProgressingProcessCode.POKEMON_BAD_REQUEST;
        } else{
            swapScheduleService.removeDeposit(exchangeid);
            code = ProgressingProcessCode.POKEMON_REQUEST_SUCCESS;
        }
        return code;
    }
    public void loadPokemonsFromDatabase(SwapScheduleService swapScheduleService){
        Consumer<SwapScheduleService> checkIfPokemArePresent = (bank) -> {
            List<PokemonDTO> pokemonsDB = pokemonRepository.getSixRandomPokemon();
            pokemonsDB.forEach(bank::addPokemonToStorage);
            checkPokemonsListSize(swapScheduleService);
        };
        checkIfPokemArePresent.accept(swapScheduleService);
    }
    private synchronized int choosePokemonToSwap(SwapScheduleService swapScheduleService) {
        checkPokemonsListSize(swapScheduleService);
        if(swapScheduleService.isPokemonStorageEmpty()){
            throw new PokemonServiceException("NOT", new Throwable("POKEMO"));
        }
        int randomPos = new Random().nextInt(0, swapScheduleService.pokemonStorageSize());
        return swapScheduleService.removeAndReturnPokemonByPosition(randomPos).getDbId();
    }
    private ProgressingProcessCode persistSwap(SwapScheduleService swapScheduleService, String exchangeId, PokemonDTO pokemonToSave, PokemonDTO pokemonToDelete){
        ProgressingProcessCode code = ProgressingProcessCode.POKEMON_REQUEST_SUCCESS;
        if (pokemonToSave != null && pokemonToDelete != null) {
            PokemonSwapDeposit deposit = swapScheduleService.getDeposit(exchangeId);
            if(deposit!= null && deposit.getPokemonToSave() != null && deposit.getPokemonToDelete() != null){
                swapScheduleService.removeDeposit(exchangeId);
                log.info("EXCHANGE WITH ID: {} HAS BEEN COMPLETED",exchangeId);
            }else{
                throw new PokemonServiceException("EXCHANGE TOOK TOO MUCH TIME TO COMPLETE");
            }
            try {
                pokemonToSave = pokemonRepository.save(pokemonToSave);
                swapScheduleService.addPokemonToStorage(pokemonToSave);
            } catch (PokemonServiceException ex) {
                pokemonRepository.deleteById(pokemonToSave.getDbId());
                swapScheduleService.removePokemonFromStorage(pokemonToSave);
                swapScheduleService.addPokemonToStorage(pokemonToDelete);
                throw new PokemonServiceException("POKEMON CANNOT BE PERSISTED");
            }
            try {
                pokemonRepository.deleteById(pokemonToDelete.getDbId());
            } catch (PokemonServiceException ex) {
                pokemonRepository.deleteById(pokemonToSave.getDbId());
                swapScheduleService.removePokemonFromStorage(pokemonToSave);
                swapScheduleService.addPokemonToStorage(pokemonToDelete);
                throw new PokemonServiceException("POKEMON CANNOT BE PERSISTED");
            }
        } else {
            code = ProgressingProcessCode.POKEMON_BAD_REQUEST;
        }
        return code;
    }
    private PokemonDTO validateAndGivePokemonToSave(PokemonRequestExchangeDTO pokemonExchangeDTO) {
        PokemonDTO pokemonToPersistDTO = null;
        if (pokemonExchangeDTO != null && !ProgressingProcessCode.POKEMON_BAD_REQUEST.equals(validatePokemonExchangeDTO(pokemonExchangeDTO))) {
            Pokemon pokemonBusiness = pokemonToExchangeMarshaller.fromDTO(pokemonExchangeDTO);
            pokemonToPersistDTO = pokemonMarshaller.toDTO(pokemonBusiness);
            normalizeDTO(pokemonToPersistDTO);
        }
        return pokemonToPersistDTO;
    }
    private ProgressingProcessCode validatePokemonExchangeDTO(PokemonRequestExchangeDTO pokemonExchangeDTO){
        if(pokemonExchangeDTO.getId() != null
                && pokemonExchangeDTO.getType() != null
                && pokemonExchangeDTO.getMoves() != null
                && !pokemonExchangeDTO.getMoves().isEmpty()
                && pokemonExchangeDTO.getMoves().stream().noneMatch(Objects::isNull)){
            Optional<PokemonSpeciesDTO> pokemonSpeciesDTO = pokemonSpeciesRepository.findByPokedexIdOrGetUnknow(pokemonExchangeDTO.getId());
            Set<MoveDTO> moves = new HashSet<>();
            pokemonExchangeDTO.getMoves().forEach(move -> {
                Optional<MoveDTO> moveDTO= moveRepository.findByPokedexIdOrGetUnknow(move);
                moveDTO.ifPresent(moves::add);
            });
            return pokemonSpeciesDTO.isPresent() && !moves.isEmpty()? ProgressingProcessCode.POKEMON_REQUEST_SUCCESS : ProgressingProcessCode.POKEMON_BAD_REQUEST;

        }
        return ProgressingProcessCode.POKEMON_BAD_REQUEST;
    }
    private PokemonRequestExchangeDTO mapPokemonToGiveForExchange(PokemonDTO pokemonDTO) {
        PokemonRequestExchangeDTO pokemonExchangeDTO = null;
        if (pokemonDTO != null) {
            Pokemon pokemonBusiness = pokemonMarshaller.fromDTO(pokemonDTO);
            pokemonExchangeDTO = pokemonToExchangeMarshaller.toDTO(pokemonBusiness);
        }
        return pokemonExchangeDTO;
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
    private void checkPokemonsListSize(SwapScheduleService swapScheduleService){
        Consumer<SwapScheduleService> checkIfPokemArePresent = (bank) -> {
            if (bank.isPokemonStorageEmpty()) {
                PokemonDTO pokemonDTO = loadPokemonFromProperty();
                bank.addPokemonToStorage(pokemonDTO);
            }
        };
        checkIfPokemArePresent.accept(swapScheduleService);
    }
    private PokemonDTO loadPokemonFromProperty() {
        PokemonDTO pokemonDTO = null;
        try {
            pokemonDTO = ObjectMapperFactory.buildStrictGenericObjectMapper().readValue(new File(pathPokemonFallBack), PokemonDTO.class);
            normalizeDTO(pokemonDTO);
            pokemonDTO = pokemonRepository.save(pokemonDTO);
        } catch (PokemonServiceException | IOException e) {
            throw new PokemonServiceException("POKEMON NOT LOADED PROPERLY FROM PROPERTY", e);
        }
        return pokemonDTO;
    }
}
