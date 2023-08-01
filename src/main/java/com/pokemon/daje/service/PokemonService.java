package com.pokemon.daje.service;

import com.pokemon.daje.model.functional.CheckValidityFunction;
import com.pokemon.daje.util.marshaller.api_marshaller.PokemonToExchangeMarshaller;
import com.pokemon.daje.util.marshaller.api_marshaller.PokemonToFrontEndMarshaller;
import com.pokemon.daje.model.*;
import com.pokemon.daje.model.api_dto.PackageExchange;
import com.pokemon.daje.model.api_dto.PackageExchangeStatus;
import com.pokemon.daje.model.api_dto.PokemonExchangeDTO;
import com.pokemon.daje.model.api_dto.PokemonFrontEndDTO;
import com.pokemon.daje.persistance.dao.MoveRepository;
import com.pokemon.daje.persistance.dao.PokemonRepository;
import com.pokemon.daje.persistance.dao.PokemonSpeciesRepository;
import com.pokemon.daje.persistance.dto.MoveDTO;
import com.pokemon.daje.persistance.dto.PokemonDTO;
import com.pokemon.daje.persistance.dto.PokemonSpeciesDTO;
import com.pokemon.daje.util.marshaller.persistance.PokemonMarshaller;
import com.pokemon.daje.util.exception.PokemonServiceException;
import io.swagger.v3.core.util.ObjectMapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
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
    private final List<PokemonDTO> swapablePokemonStorage;
    private final Map<String, PokemonSwapDeposit> swapBank;
    private final Map<String, PokemonSwapDeposit> swapCacheLog;
    private final DataSource dataSource;
    @Value("${pokemon.fallback.path}")
    private String pathPokemonFallBack;
    private CheckValidityFunction validatePokemon;

    @Autowired
    public PokemonService(PokemonRepository pokemonRepository,
                          PokemonMarshaller pokemonMarshaller,
                          PokemonToFrontEndMarshaller pokemonToFrontEndMarshaller,
                          PokemonToExchangeMarshaller pokemonToExchangeMarshaller,
                          MoveRepository moveRepository,
                          PokemonSpeciesRepository pokemonSpeciesRepository) {

        this.pokemonRepository = pokemonRepository;
        this.pokemonMarshaller = pokemonMarshaller;
        this.pokemonToFrontEndMarshaller = pokemonToFrontEndMarshaller;
        this.pokemonToExchangeMarshaller = pokemonToExchangeMarshaller;
        this.moveRepository = moveRepository;
        this.pokemonSpeciesRepository = pokemonSpeciesRepository;
        this.swapablePokemonStorage = new ArrayList<>(pokemonRepository.getSixRandomPokemon());
        this.swapBank = new HashMap<>();
        this.dataSource = DataSourceBuilder.create()
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .url("jdbc:mysql://localhost:3306/daje")
                .username("daje")
                .password("daje")
                .build();
        this.swapCacheLog = new HashMap<>();
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

    public List<PokemonFrontEndDTO> getPokemonInStorage(){
        checkPokemonsListSize();
        return swapablePokemonStorage.stream().map(pokemonDTO -> {
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

    public PackageExchange inizializeSwap(PokemonExchangeDTO pokemon) {
        PackageExchange exchangeSwapDTO = null;
            PokemonDTO pokemonToPersistDTO = normalizeAndGivePokemonToSave(pokemon);
            PokemonDTO pokemonToGive = choosePokemonToSwap();
            Set<MoveDTO> pokemonToGiveMoves = moveRepository.findAllByPokemonId(pokemonToGive.getDbId());
            pokemonToGive.setMoveSet(pokemonToGiveMoves);
            if(pokemonToPersistDTO != null){
                String idSwap = UUID.randomUUID().toString();
                exchangeSwapDTO = new PackageExchange(idSwap, mapPokemonToGiveForSwap(pokemonToGive));
                swapCacheLog.put(idSwap,
                        new PokemonSwapDeposit(
                                Map.of(
                                        SwapBankAction.TOSAVE, pokemonToPersistDTO,
                                        SwapBankAction.TODELETE, pokemonToGive
                                )
                        )
                );
                swapBank.put(idSwap,
                        new PokemonSwapDeposit(
                                Map.of(
                                        SwapBankAction.TOSAVE, pokemonToPersistDTO,
                                        SwapBankAction.TODELETE, pokemonToGive
                                )
                        )
                );
        }
        return exchangeSwapDTO;
    }

    public ProgressingProcessCode concludeSwap(String exchangeid, PackageExchangeStatus packageExchangeStatus) {
        ProgressingProcessCode code = ProgressingProcessCode.POKEMON_REQUEST_DOWN_SERVER_ERROR;
        if (packageExchangeStatus.getStatus() == ProgressingProcessCode.POKEMON_REQUEST_SUCCESS.getCode() && swapBank.containsKey(exchangeid)) {
            PokemonSwapDeposit exchange = swapBank.get(exchangeid);
            PokemonDTO pokemonToSave = exchange != null ? exchange.getPokemonToSave() : null;
            PokemonDTO pokemonToDelete = exchange != null ? exchange.getPokemonToDelete() : null;
            try{
                code = progressWithSwap(pokemonToSave, pokemonToDelete);
                swapBank.remove(exchangeid);
            }catch (PokemonServiceException exception){
                swapBank.remove(exchangeid);
                log.info("ERROR TRYING TO ASSOCIATE VARIABLE CODE TO RETURN VALUE OF PROGRESSWITHSWAP METHOD");
            }
        }else if((!swapBank.containsKey(exchangeid))){
            code = ProgressingProcessCode.POKEMON_EXCHANGE_NOT_FOUND;
        }else {
            swapBank.remove(exchangeid);
            code = ProgressingProcessCode.POKEMON_REQUEST_SUCCESS;
        }
        return code;
    }

    public PokemonDTO choosePokemonToSwap() {
        checkPokemonsListSize();
        Random random = new Random();
        int randomPos = random.nextInt(0, swapablePokemonStorage.size());
        return swapablePokemonStorage.remove(randomPos);
    }

    private void normalizeDTO(PokemonDTO pokemonToNormalize) {
        if(pokemonToNormalize != null){
            Set<MoveDTO> movesDTO = new HashSet<>();
            Optional<PokemonSpeciesDTO> speciesDTO =
                    pokemonSpeciesRepository.findByPokedexIdOrGetUnknow(pokemonToNormalize.getPokemonSpeciesDTO().getPokedexId());
            pokemonToNormalize.getMoveSet().forEach(move -> {
                Optional<MoveDTO> moveDTO = moveRepository.findByPokedexIdOrGetUnknow(move.getPokedexId());
                moveDTO.ifPresent(movesDTO::add);
            });
            speciesDTO.ifPresent(pokemonToNormalize::setPokemonSpeciesDTO);
            pokemonToNormalize.setMoveSet(movesDTO);
        }
    }

    private PokemonDTO normalizeAndGivePokemonToSave(PokemonExchangeDTO pokemonExchangeDTO) {
        PokemonDTO pokemonToPersistDTO = null;
            Pokemon pokemonBusiness = pokemonToExchangeMarshaller.fromDTO(pokemonExchangeDTO);
            pokemonToPersistDTO = pokemonMarshaller.toDTO(pokemonBusiness);
            normalizeDTO(pokemonToPersistDTO);
        return pokemonToPersistDTO;
    }

    private PokemonExchangeDTO mapPokemonToGiveForSwap(PokemonDTO pokemonDTO) {
        PokemonExchangeDTO pokemonExchangeDTO = null;
        if (pokemonDTO != null) {
            Pokemon pokemonBusiness = pokemonMarshaller.fromDTO(pokemonDTO);
            pokemonExchangeDTO = pokemonToExchangeMarshaller.toDTO(pokemonBusiness);
        }
        return pokemonExchangeDTO;
    }

    private ProgressingProcessCode progressWithSwap(PokemonDTO pokemonToSave, PokemonDTO pokemonToDelete) throws PokemonServiceException {
        ProgressingProcessCode code = ProgressingProcessCode.POKEMON_REQUEST_SUCCESS;
        try{
            if (pokemonToSave != null && pokemonToDelete != null) {
                pokemonToSave = pokemonRepository.save(pokemonToSave);
                swapablePokemonStorage.add(pokemonToSave);
                pokemonRepository.deleteById(pokemonToDelete.getDbId());
            } else {
                code = ProgressingProcessCode.POKEMON_BAD_REQUEST;
            }
        }catch (Exception ex){
            pokemonRepository.deleteById(pokemonToSave.getDbId());
            swapablePokemonStorage.remove(pokemonToSave);
            pokemonRepository.save(pokemonToDelete);
            swapablePokemonStorage.add(pokemonToDelete);
            log.error("POKEMON CANNOT BE PERSISTED");
            throw new PokemonServiceException("ERROR WHILE POKEMON SWAP WAS ABOUT TO BE PERSISTED");
        }
        return code;
    }
    
    private void checkPokemonsListSize(){
        Consumer<List<PokemonDTO>> checkIfPokemArePresent = pokemonList -> {
            try{
                if (pokemonList.isEmpty()) {
                    pokemonList.add(loadPokemonFromProperty());
                }
            }catch (PokemonServiceException exception){
                log.error("FAILED TO LOAD POKEMON FALLBACK");
            }
        };
        checkIfPokemArePresent.accept(swapablePokemonStorage);
    }

    private PokemonDTO loadPokemonFromProperty() throws PokemonServiceException {
        PokemonDTO pokemonDTO;
        try {
            pokemonDTO = ObjectMapperFactory.buildStrictGenericObjectMapper().readValue(new File(pathPokemonFallBack), PokemonDTO.class);
            normalizeDTO(pokemonDTO);
            pokemonDTO = pokemonRepository.save(pokemonDTO);
        } catch (Exception e) {
            throw new PokemonServiceException("POKEMON NOT CORRECTLY LOADED FROM PROPERTY");
        }
        return pokemonDTO;
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
            if(swapablePokemonStorage.size() < 6){
                swapablePokemonStorage.add(deposit.getPokemonToDelete());
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
        spoiledExchangeCache.forEach(swapCacheLog::remove);
    }

//    @Scheduled(fixedDelay = 2000)
//    private void checkDatabaseConnection() {
//        try (Connection connection = dataSource.getConnection()) {
//            log.info("DATABASE CONNECTION WORKING");
//        } catch (Exception e) {
//            log.error("DATABASE CONNECTION NOT WORKING");
//            System.exit(1);
//        }
//    }

    public Map<SwapBankAction,PokemonFrontEndDTO> getPokemonsFromSwapCacheLog(String exchangeId){
        PokemonSwapDeposit deposit = swapCacheLog.get(exchangeId);
        PokemonFrontEndDTO toSave;
        PokemonFrontEndDTO toDelete;
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
