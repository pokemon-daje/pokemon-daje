package com.pokemon.daje.service;

import com.pokemon.daje.model.api_objects.PackageExchangeStatus;
import com.pokemon.daje.model.api_objects.PackageResponseExchange;
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
public class SwapBankLogistic {
    private final PokemonRepository pokemonRepository;
    private final PokemonMarshaller pokemonMarshaller;
    private final PokemonToExchangeMarshaller pokemonToExchangeMarshaller;
    private final MoveRepository moveRepository;
    private final PokemonSpeciesRepository pokemonSpeciesRepository;
    @Value("${pokemon.fallback.path}")
    private String pathPokemonFallBack;

    public SwapBankLogistic(PokemonRepository pokemonRepository,
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

    public PackageResponseExchange inizializePokemonsSwap(SwapBankService swapBankService,PokemonRequestExchangeDTO pokemon) {
        PackageResponseExchange exchangeSwapDTO = null;
        if (pokemon != null) {
            PokemonDTO pokemonToPersistDTO = validateAndGivePokemonToSave(pokemon);
            int pokemonToGiveId = choosePokemonToSwap(swapBankService);
            Optional<PokemonDTO> pokemonDTOToGive= pokemonRepository.findById(pokemonToGiveId);
            if(pokemonDTOToGive.isPresent() && pokemonToPersistDTO != null){
                String idSwap = UUID.randomUUID().toString();
                swapBankService.addDeposit(idSwap,pokemonToPersistDTO,pokemonDTOToGive.get());
                Set<MoveDTO> moveDTOSet = moveRepository.findAllByPokemonId(pokemonToGiveId);
                pokemonDTOToGive.get().setMoveSet(moveDTOSet);
                exchangeSwapDTO = new PackageResponseExchange(idSwap, mapPokemonToGiveForExchange(pokemonDTOToGive.get()));
            }
        }
        return exchangeSwapDTO;
    }
    public ProgressingProcessCode concludeSwap(SwapBankService swapBankService, String exchangeid, PackageExchangeStatus packageExchangeStatus) {
        ProgressingProcessCode code;
        if (packageExchangeStatus != null
                && packageExchangeStatus.getStatus() != null
                && packageExchangeStatus.getStatus() == ProgressingProcessCode.POKEMON_REQUEST_SUCCESS.getCode()
                && swapBankService.doDepositExist(exchangeid)) {
            PokemonSwapDeposit exchange = swapBankService.getDeposit(exchangeid);
            PokemonDTO pokemonToSave = exchange != null ? exchange.getPokemonToSave() : null;
            PokemonDTO pokemonToDelete = exchange != null ? exchange.getPokemonToDelete() : null;

            code = persistSwap(swapBankService,exchangeid,pokemonToSave, pokemonToDelete);
        }else if((packageExchangeStatus.getStatus() == ProgressingProcessCode.POKEMON_REQUEST_SUCCESS.getCode()
                || packageExchangeStatus.getStatus() == ProgressingProcessCode.POKEMON_BAD_REQUEST.getCode()
                || packageExchangeStatus.getStatus() == ProgressingProcessCode.POKEMON_REQUEST_DOWN_SERVER_ERROR.getCode())
                && !swapBankService.doDepositExist(exchangeid)){
            code = ProgressingProcessCode.POKEMON_EXCHANGE_NOT_FOUND;
        } else if (ProgressingProcessCode.fromNumber(packageExchangeStatus.getStatus()).equals(ProgressingProcessCode.POKEMON_REQUEST_UNKWON)) {
            swapBankService.removeDeposit(exchangeid);
            code = ProgressingProcessCode.POKEMON_EXCHANGE_NOT_FOUND;
        } else{
            swapBankService.removeDeposit(exchangeid);
            code = ProgressingProcessCode.POKEMON_REQUEST_SUCCESS;
        }
        return code;
    }
    public void loadPokemonsFromDatabase(SwapBankService swapBankService){
        Consumer<SwapBankService> checkIfPokemArePresent = (bank) -> {
            List<PokemonDTO> pokemonsDB = pokemonRepository.getSixRandomPokemon();
            pokemonsDB.forEach(poke->{
                bank.addPokemonToStorage(poke);
            });
            checkPokemonsListSize(swapBankService);
        };
        checkIfPokemArePresent.accept(swapBankService);
    }
    private synchronized int choosePokemonToSwap(SwapBankService swapBankService) {
        checkPokemonsListSize(swapBankService);
        Random random = new Random();
        PokemonDTO pokemonSwap = null;
        if (!swapBankService.isPokemonStorageEmpty()) {
            int randomPos = random.nextInt(0, swapBankService.pokemonStorageSize());
            pokemonSwap = swapBankService.removeAndReturnPokemonByPosition(randomPos);
        }
        return pokemonSwap.getDbId();
    }
    private ProgressingProcessCode persistSwap(SwapBankService swapBankService, String exchangeId, PokemonDTO pokemonToSave, PokemonDTO pokemonToDelete){
        ProgressingProcessCode code = ProgressingProcessCode.POKEMON_REQUEST_SUCCESS;
        if (pokemonToSave != null && pokemonToDelete != null) {
            try{
                PokemonSwapDeposit deposit = swapBankService.getDeposit(exchangeId);
                if(deposit!= null && deposit.getPokemonToSave() != null && deposit.getPokemonToDelete() != null){
                    swapBankService.removeDeposit(exchangeId);
                    log.info("EXCHANGE WITH ID: {} HAS BEEN COMPLETED",exchangeId);
                }else{
                    throw new PokemonServiceException("EXCHANGE TOOK TOO MUCH TIME TO COMPLETE",new Throwable("POKEMON TO PERSIST"));
                }
            } catch (PokemonServiceException ex) {
                code = ProgressingProcessCode.POKEMON_BAD_REQUEST;
                throw new PokemonServiceException("EXCHANGE TOOK TOO MUCH TIME TO COMPLETE", ex);
            }
            try {
                pokemonToSave = pokemonRepository.save(pokemonToSave);
                swapBankService.addPokemonToStorage(pokemonToSave);
            } catch (PokemonServiceException ex) {
                code = ProgressingProcessCode.POKEMON_REQUEST_DOWN_SERVER_ERROR;
                throw new PokemonServiceException("Pokemon cannot be persisted", ex);
            }
            try {
                pokemonRepository.deleteById(pokemonToDelete.getDbId());
            } catch (PokemonServiceException ex) {
                pokemonRepository.deleteById(pokemonToSave.getDbId());
                swapBankService.removePokemonFromStorage(pokemonToSave);
                swapBankService.addPokemonToStorage(pokemonToDelete);
                code = ProgressingProcessCode.POKEMON_REQUEST_DOWN_SERVER_ERROR;
                throw new PokemonServiceException("POKEMON CANNOT BE PERSISTED", ex);
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
                && !pokemonExchangeDTO.getMoves().stream().anyMatch(Objects::isNull)){
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
    private void checkPokemonsListSize(SwapBankService swapBankService){
        Consumer<SwapBankService> checkIfPokemArePresent = (bank) -> {
            if (bank.isPokemonStorageEmpty()) {
                PokemonDTO pokemonDTO = loadPokemonFromProperty();
                bank.addPokemonToStorage(pokemonDTO);
            }
        };
        checkIfPokemArePresent.accept(swapBankService);
    }
    private PokemonDTO loadPokemonFromProperty() {
        PokemonDTO pokemonDTO = null;
        try {
            pokemonDTO = ObjectMapperFactory.buildStrictGenericObjectMapper().readValue(new File(pathPokemonFallBack), PokemonDTO.class);
            normalizeDTO(pokemonDTO);
            pokemonDTO = pokemonRepository.save(pokemonDTO);
        } catch (PokemonServiceException | IOException e) {
            throw new PokemonServiceException("pokemon not loaded properly from propert", e);
        }
        return pokemonDTO;
    }
}
