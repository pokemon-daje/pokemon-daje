package com.pokemon.daje.service;

import com.pokemon.daje.controller.json.dto.*;
import com.pokemon.daje.controller.json.marshaller.PokemonToExchangeMarshaller;
import com.pokemon.daje.controller.json.marshaller.PokemonToFrontEndMarshaller;
import com.pokemon.daje.model.*;
import com.pokemon.daje.persistance.dao.MoveRepository;
import com.pokemon.daje.persistance.dao.PokemonRepository;
import com.pokemon.daje.persistance.dao.PokemonSpeciesRepository;
import com.pokemon.daje.persistance.dao.TypeRepository;
import com.pokemon.daje.persistance.dto.MoveDTO;
import com.pokemon.daje.persistance.dto.PokemonDTO;
import com.pokemon.daje.persistance.dto.PokemonSpeciesDTO;
import com.pokemon.daje.persistance.dto.TypeDTO;
import com.pokemon.daje.persistance.marshaller.PokemonMarshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PokemonService {
    private final PokemonRepository pokemonRepository;
    private final PokemonMarshaller pokemonMarshaller;
    private final PokemonToFrontEndMarshaller pokemonToFrontEndMarshaller;
    private final PokemonToExchangeMarshaller pokemonToExchangeMarshaller;
    private final TypeRepository typeRepository;
    private final MoveRepository moveRepository;
    private final PokemonSpeciesRepository pokemonSpeciesRepository;
    private final List<PokemonDTO> randomPokemonStorage;
    private Map<String, PokemonSwapDeposit> swapBank;

    @Autowired
    public PokemonService(PokemonRepository pokemonRepository,
                          PokemonMarshaller pokemonMarshaller,
                          PokemonToFrontEndMarshaller pokemonToFrontEndMarshaller,
                          PokemonToExchangeMarshaller pokemonToExchangeMarshaller,
                          TypeRepository typeRepository, MoveRepository moveRepository,
                          PokemonSpeciesRepository pokemonSpeciesRepository) {

        this.pokemonRepository = pokemonRepository;
        this.pokemonMarshaller = pokemonMarshaller;
        this.pokemonToFrontEndMarshaller = pokemonToFrontEndMarshaller;
        this.pokemonToExchangeMarshaller = pokemonToExchangeMarshaller;
        this.typeRepository = typeRepository;
        this.moveRepository = moveRepository;
        this.pokemonSpeciesRepository = pokemonSpeciesRepository;
        this.randomPokemonStorage = new ArrayList<>(pokemonRepository.getSixRandomPokemon());
        this.swapBank = new HashMap<>();
    }

    public PokemonFrontEndDTO getById(int pokemonId) {
        Pokemon pokemonBusiness = pokemonMarshaller.fromDTO(pokemonRepository.findById(pokemonId).orElse(null));
        return pokemonToFrontEndMarshaller.toDTO(pokemonBusiness);
    }

    public PokemonDTO insert(Pokemon pokemon) {
        if (pokemon.getType() != null) {
            PokemonDTO pokemonDTO = pokemonMarshaller.toDTO(pokemon);
            normalizeDTO(pokemonDTO);
            return pokemonRepository.save(pokemonDTO);
        }
        return null;
    }

    public void deleteById(int id) {
        pokemonRepository.deleteById(id);
    }

    public List<PokemonFrontEndDTO> getSixRandomPokemon() {
        List<PokemonDTO> pokemonDTOList = pokemonRepository.getSixRandomPokemon();
        return pokemonDTOList.stream().map(pokemonDTO -> {
            int databaseId = pokemonDTO.getDbId();
            Pokemon businessPokemon = pokemonMarshaller.fromDTO(pokemonDTO);
            PokemonFrontEndDTO pokemonFrontEndDTO = pokemonToFrontEndMarshaller.toDTO(businessPokemon);
            pokemonFrontEndDTO.setDatabaseId(databaseId);
            return pokemonFrontEndDTO;
        }).toList();
    }

    public PackageExchange inizializePokemonsSwap(PokemonExchangeDTO pokemon) {
        PackageExchange exchangeSwapDTO = null;
        if (pokemon != null) {
            PokemonDTO pokemonToPersistDTO = validateAndGivePokemonToSave(pokemon);
            int pokemonToGiveId = choosePokemonToSwap();
            Optional<PokemonDTO> pokemonDTOToGive= pokemonRepository.findById(pokemonToGiveId);
            if(pokemonDTOToGive.isPresent()){
                normalizeDTO(pokemonDTOToGive.get());
                String idSwap = UUID.randomUUID().toString();
                exchangeSwapDTO = new PackageExchange(idSwap, mapPokemonToGiveForExchange(pokemonDTOToGive.get()));
                swapBank.put(idSwap,
                        new PokemonSwapDeposit(
                                Map.of(
                                        SwapBankAction.TOSAVE, pokemonToPersistDTO,
                                        SwapBankAction.TODELETE, pokemonDTOToGive.get()
                                )
                        )
                );
            }
        }
        return exchangeSwapDTO;
    }

    public int nextStepSwap(String exchangeid, PackageExchangeStatus packageExchangeStatus) {
        int code = 0;
        if (packageExchangeStatus.getStatus() == ResponseCode.SUCCESS.getCode()) {
            PokemonSwapDeposit exchange = swapBank.get(exchangeid);
            PokemonDTO pokemonToSave = exchange != null ? exchange.getPokemonToSave() : null;
            PokemonDTO pokemonToDelete = exchange != null ? exchange.getPokemonToDelete() : null;

            if (pokemonToSave != null && pokemonToDelete != null) {
                try {
                    pokemonToSave = pokemonRepository.save(pokemonToSave);
                } catch (Exception ex) {
                    return ResponseCode.SERVER_ERROR.getCode();
                }
                try {
                    pokemonRepository.delete(pokemonToDelete);
                } catch (Exception ex) {
                    pokemonRepository.delete(pokemonToSave);
                    return ResponseCode.SERVER_ERROR.getCode();
                }
                code = ResponseCode.SUCCESS.getCode();
            } else {
                code = ResponseCode.BAD_REQUEST.getCode();
            }
            swapBank.remove(exchangeid);
        }
        return code;
    }

    public int choosePokemonToSwap() {
        Random random = new Random();
        PokemonDTO pokemonSwap = null;
        if (!randomPokemonStorage.isEmpty()) {
            int randomPos = random.nextInt(0, randomPokemonStorage.size());
            pokemonSwap = randomPokemonStorage.remove(randomPos);
        }
        return pokemonSwap.getDbId();
    }

    private void normalizeDTO(PokemonDTO pokemonToNormalize) {
        Set<MoveDTO> movesDTO = new HashSet<>();
        Optional<PokemonSpeciesDTO> speciesDTO = pokemonSpeciesRepository.findByPokedexId(pokemonToNormalize.getPokemonSpeciesDTO().getPokedexId());
        pokemonToNormalize.getMoveSet().forEach(move -> {
            Optional<MoveDTO> moveDTO = moveRepository.findByPokedexId(move.getPokedexId());
            moveDTO.ifPresent(movesDTO::add);
        });
        speciesDTO.ifPresent(pokemonToNormalize::setPokemonSpeciesDTO);
        pokemonToNormalize.setMoveSet(movesDTO);
    }

    private ResponseCode validatePokemonExchangeDTO(PokemonExchangeDTO pokemonExchangeDTO){
        int code = 0;
        Optional<PokemonSpeciesDTO> pokemonSpeciesDTO = pokemonSpeciesRepository.findByPokedexId(pokemonExchangeDTO.getId());
        Set<MoveDTO> moves = new HashSet<>();
        pokemonExchangeDTO.getMoves().forEach(move -> {
            Optional<MoveDTO> moveDTO= moveRepository.findByPokedexIdOrGetUnknow(move);
            moveDTO.ifPresent(moves::add);
        });
        return pokemonSpeciesDTO.isPresent() && !moves.isEmpty()? ResponseCode.SUCCESS : ResponseCode.BAD_REQUEST;
    }

    private PokemonDTO validateAndGivePokemonToSave(PokemonExchangeDTO pokemonExchangeDTO) {
        PokemonDTO pokemonToPersistDTO = null;
        if (pokemonExchangeDTO != null && !ResponseCode.BAD_REQUEST.equals(validatePokemonExchangeDTO(pokemonExchangeDTO))) {
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

    @Scheduled(fixedDelay = 30000)
    private void checkTimeBank() {
        List<String> spoiledExchange = new ArrayList<>();
        swapBank.forEach((key, exchange) -> {
            if (System.currentTimeMillis() - exchange.getDepositTime() > 5000) {
                spoiledExchange.add(key);
            }
        });
        spoiledExchange.forEach(key -> swapBank.remove(key));
    }
}
