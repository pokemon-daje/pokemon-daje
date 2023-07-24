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
import com.pokemon.daje.util.RandomPokemonStorage;
import org.springframework.beans.factory.annotation.Autowired;
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
        if (pokemon.getType() != null && !Types.fromString(pokemon.getType().getName()).equals(Types.UNKNOWN)) {
            PokemonDTO pokemonDTO = pokemonMarshaller.toDTO(pokemon);
            Optional<TypeDTO> pokemonType = getTypeDTO(pokemon.getType().getId());
            Set<MoveDTO> alteredMoves = new HashSet<>();
            pokemonDTO.getMoveSet().forEach(moveDTO -> {
                Optional<MoveDTO> moveInDB = getMoveDTO(moveDTO.getPokedexId());
                if(moveInDB.isPresent()){
                    alteredMoves.add(moveInDB.get());
                }else{
                    Optional<TypeDTO> optionalMoveType = getTypeDTO(moveDTO.getType().getPokedexId());
                    optionalMoveType.ifPresent(moveType -> {
                        moveDTO.setType(moveType);
                        alteredMoves.add(moveDTO);
                    });
                }
            });
            if (pokemonType.isPresent() && alteredMoves.size() == pokemonDTO.getMoveSet().size()) {
                Optional<PokemonSpeciesDTO> specie = getSpecieDTO(pokemon.getId());
                if(specie.isEmpty()){
                    pokemonDTO.getPokemonSpeciesDTO().setType(pokemonType.get());
                }
                specie.ifPresent(pokemonDTO::setPokemonSpeciesDTO);
                pokemonDTO.setMoveSet(alteredMoves);
                return pokemonRepository.save(pokemonDTO);
            }
        }
        return null;
    }

    public void deleteById(int id) {
        pokemonRepository.deleteById(id);
    }

    public List<Pokemon> getSixRandomPokemon() {
        List<PokemonDTO> pokemonDTOList = pokemonRepository.getSixRandomPokemon();
        return pokemonDTOList.stream().map(pokemonMarshaller::fromDTO).toList();
    }

    public Pokemon swap(Pokemon pokemon) {
        Pokemon pokemonToReturn = null;
        if (pokemon != null) {
            PokemonDTO pokemonSaved = insert(pokemon);
            PokemonDTO dtoSwap = randomPokemonStorage.swapPokemon(pokemonSaved);
            Optional<PokemonDTO> optPokeToDelete = pokemonRepository.findById(dtoSwap.getDbId());
            pokemonToReturn = pokemonMarshaller.fromDTO(optPokeToDelete.get());
            pokemonRepository.delete(optPokeToDelete.get());
            listOfChagedPokemon.add(pokemonToReturn);
        }
        return pokemonToReturn;
    }

    public List<Pokemon> getListOfChagedPokemon(){
        List<Pokemon> changedPokemon = new ArrayList<>(listOfChagedPokemon);
        listOfChagedPokemon = new ArrayList<>();
        return changedPokemon;
    }

    private Optional<MoveDTO> getMoveDTO(int pokedexId){
        return moveRepository.findByPokedexId(pokedexId);
    }

    private Optional<TypeDTO> getTypeDTO(int pokedexId){
        return typeRepository.findByPokedexId(pokedexId);
    }

    private Optional<PokemonSpeciesDTO> getSpecieDTO(int pokedexId){
        return pokemonSpeciesRepository.findByPokedexId(pokedexId);
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
