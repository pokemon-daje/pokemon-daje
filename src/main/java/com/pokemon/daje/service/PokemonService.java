package com.pokemon.daje.service;

import com.pokemon.daje.controller.json.dto.*;
import com.pokemon.daje.controller.json.marshaller.PokemonToExchangeMarshaller;
import com.pokemon.daje.controller.json.marshaller.PokemonToFrontEndMarshaller;
import com.pokemon.daje.model.*;
import com.pokemon.daje.persistance.dao.MoveRepository;
import com.pokemon.daje.persistance.dao.PokemonRepository;
import com.pokemon.daje.persistance.dao.PokemonSpeciesRepository;
import com.pokemon.daje.persistance.dto.MoveDTO;
import com.pokemon.daje.persistance.dto.PokemonDTO;
import com.pokemon.daje.persistance.dto.PokemonSpeciesDTO;
import com.pokemon.daje.persistance.marshaller.PokemonMarshaller;
import com.pokemon.daje.util.exception.PokemonServiceException;
import io.swagger.v3.core.util.ObjectMapperFactory;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.*;
import java.util.function.Consumer;

@Slf4j
@Service
public class PokemonService {
    private final PokemonRepository pokemonRepository;
    private final PokemonMarshaller pokemonMarshaller;
    private final PokemonToFrontEndMarshaller pokemonToFrontEndMarshaller;
    private final PokemonToExchangeMarshaller pokemonToExchangeMarshaller;
    private final MoveRepository moveRepository;
    private final PokemonSpeciesRepository pokemonSpeciesRepository;
    private final SwapBank swapBank;
    private DataSource dataSource;
    @Value("${pokemon.fallback.path}")
    private String pathPokemonFallBack;

    @Autowired
    public PokemonService(PokemonRepository pokemonRepository,
                          PokemonMarshaller pokemonMarshaller,
                          PokemonToFrontEndMarshaller pokemonToFrontEndMarshaller,
                          PokemonToExchangeMarshaller pokemonToExchangeMarshaller,
                          MoveRepository moveRepository,
                          PokemonSpeciesRepository pokemonSpeciesRepository,
                          SwapBank swapBank) {
        this.pokemonRepository = pokemonRepository;
        this.pokemonMarshaller = pokemonMarshaller;
        this.pokemonToFrontEndMarshaller = pokemonToFrontEndMarshaller;
        this.pokemonToExchangeMarshaller = pokemonToExchangeMarshaller;
        this.moveRepository = moveRepository;
        this.pokemonSpeciesRepository = pokemonSpeciesRepository;
        this.swapBank = swapBank;
        this.dataSource = DataSourceBuilder.create()
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .url("jdbc:mysql://localhost:3306/daje")
                .username("daje")
                .password("daje")
                .build();
        loadPokemonsFromDatabase();
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
        return swapBank.getPokemonStorage().stream().map(pokemonDTO -> {
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

    public PackageExchange inizializePokemonsSwap(PokemonExchangeDTO pokemon, HttpServletResponse response) {
        PackageExchange exchangeSwapDTO = null;
        if (pokemon != null) {
            PokemonDTO pokemonToPersistDTO = validateAndGivePokemonToSave(pokemon);
            int pokemonToGiveId = choosePokemonToSwap();
            Optional<PokemonDTO> pokemonDTOToGive= pokemonRepository.findById(pokemonToGiveId);
            if(pokemonDTOToGive.isPresent() && pokemonToPersistDTO != null){
                String idSwap = UUID.randomUUID().toString();
                swapBank.addDeposit(idSwap,pokemonToPersistDTO,pokemonDTOToGive.get());
                exchangeSwapDTO = new PackageExchange(idSwap, mapPokemonToGiveForExchange(pokemonDTOToGive.get()));
            }
        }
        return exchangeSwapDTO;
    }

    public ProgressingProcessCode nextStepSwap(String exchangeid, PackageExchangeStatus packageExchangeStatus) {
        ProgressingProcessCode code;
        if (packageExchangeStatus.getStatus() == ProgressingProcessCode.POKEMON_REQUEST_SUCCESS.getCode() && swapBank.doDepositExist(exchangeid)) {
            PokemonSwapDeposit exchange = swapBank.getDeposit(exchangeid);
            PokemonDTO pokemonToSave = exchange != null ? exchange.getPokemonToSave() : null;
            PokemonDTO pokemonToDelete = exchange != null ? exchange.getPokemonToDelete() : null;

            code = progressWithSwap(exchangeid,pokemonToSave, pokemonToDelete);
        }else if((packageExchangeStatus.getStatus() == ProgressingProcessCode.POKEMON_REQUEST_SUCCESS.getCode()
                || packageExchangeStatus.getStatus() == ProgressingProcessCode.POKEMON_BAD_REQUEST.getCode()
                || packageExchangeStatus.getStatus() == ProgressingProcessCode.POKEMON_REQUEST_DOWN_SERVER_ERROR.getCode())
                && !swapBank.doDepositExist(exchangeid)){
            code = ProgressingProcessCode.POKEMON_EXCHANGE_NOT_FOUND;
        }else{
            swapBank.removeDeposit(exchangeid);
            code = ProgressingProcessCode.POKEMON_REQUEST_SUCCESS;
        }
        return code;
    }

    public int choosePokemonToSwap() {
        checkPokemonsListSize();
        Random random = new Random();
        PokemonDTO pokemonSwap = null;
        if (!swapBank.isPokemonStorageEmpty()) {
            int randomPos = random.nextInt(0, swapBank.pokemonStorageSize());
            pokemonSwap = swapBank.removeAndReturnPokemonByPosition(randomPos);
        }
        return pokemonSwap.getDbId();
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

    private ProgressingProcessCode validatePokemonExchangeDTO(PokemonExchangeDTO pokemonExchangeDTO){
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

    private PokemonDTO validateAndGivePokemonToSave(PokemonExchangeDTO pokemonExchangeDTO) {
        PokemonDTO pokemonToPersistDTO = null;
        if (pokemonExchangeDTO != null && !ProgressingProcessCode.POKEMON_BAD_REQUEST.equals(validatePokemonExchangeDTO(pokemonExchangeDTO))) {
            Pokemon pokemonBusiness = pokemonToExchangeMarshaller.fromDTO(pokemonExchangeDTO);
            pokemonToPersistDTO = pokemonMarshaller.toDTO(pokemonBusiness);
            normalizeDTO(pokemonToPersistDTO);
        }
        return pokemonToPersistDTO;
    }

    private PokemonExchangeDTO mapPokemonToGiveForExchange(PokemonDTO pokemonDTO) {
        PokemonExchangeDTO pokemonExchangeDTO = null;
        if (pokemonDTO != null) {
            Pokemon pokemonBusiness = pokemonMarshaller.fromDTO(pokemonDTO);
            pokemonExchangeDTO = pokemonToExchangeMarshaller.toDTO(pokemonBusiness);
        }
        return pokemonExchangeDTO;
    }

    private ProgressingProcessCode progressWithSwap(String exchangeId,PokemonDTO pokemonToSave, PokemonDTO pokemonToDelete){
        ProgressingProcessCode code = ProgressingProcessCode.POKEMON_REQUEST_SUCCESS;
        if (pokemonToSave != null && pokemonToDelete != null) {
            try{
                PokemonSwapDeposit deposit = swapBank.getDeposit(exchangeId);
                if(deposit!= null && deposit.getPokemonToSave() != null && deposit.getPokemonToDelete() != null){
                    swapBank.removeDeposit(exchangeId);
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
                swapBank.addPokemonToStorage(pokemonToSave);
            } catch (PokemonServiceException ex) {
                code = ProgressingProcessCode.POKEMON_REQUEST_DOWN_SERVER_ERROR;
                throw new PokemonServiceException("Pokemon cannot be persisted", ex);
            }
            try {
                pokemonRepository.deleteById(pokemonToDelete.getDbId());
            } catch (PokemonServiceException ex) {
                pokemonRepository.deleteById(pokemonToSave.getDbId());
                swapBank.removePokemonFromStorage(pokemonToSave);
                swapBank.addPokemonToStorage(pokemonToDelete);
                code = ProgressingProcessCode.POKEMON_REQUEST_DOWN_SERVER_ERROR;
                throw new PokemonServiceException("POKEMON CANNOT BE PERSISTED", ex);
            }
        } else {
            code = ProgressingProcessCode.POKEMON_BAD_REQUEST;
        }
        return code;
    }
    
    private void checkPokemonsListSize(){
        Consumer<SwapBank> checkIfPokemArePresent = (bank) -> {
            if (bank.isPokemonStorageEmpty()) {
                PokemonDTO pokemonDTO = loadPokemonFromProperty();
                bank.addPokemonToStorage(pokemonDTO);
            }
        };
        checkIfPokemArePresent.accept(swapBank);
    }

    private void loadPokemonsFromDatabase(){
        Consumer<SwapBank> checkIfPokemArePresent = (bank) -> {
            List<PokemonDTO> pokemonsDB = pokemonRepository.getSixRandomPokemon();
            pokemonsDB.forEach(poke->{
                bank.addPokemonToStorage(poke);
            });
            checkPokemonsListSize();
        };
        checkIfPokemArePresent.accept(swapBank);
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

    @Scheduled(fixedDelay = 2000)
    private void checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            log.info("database connesso e funzionante");
        } catch (Exception e) {
            log.info("database non connesso o non funzionante");
            System.exit(1);
        }
    }

    public Map<SwapBankAction,PokemonFrontEndDTO> getPokemonsFromSwapCacheLog(String exchangeId){
        PokemonSwapDeposit deposit = swapBank.getCacheOfDeposit(exchangeId);
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


}
