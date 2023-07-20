package com.pokemon.daje.service;

import com.pokemon.daje.model.Move;
import com.pokemon.daje.model.Pokemon;
import com.pokemon.daje.model.Types;
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
    private final TypeRepository typeRepository;
    private final MoveRepository moveRepository;
    private final PokemonSpeciesRepository pokemonSpeciesRepository;
    private final RandomPokemonStorage randomPokemonStorage;
    private List<Pokemon> listOfChagedPokemon;

    @Autowired
    public PokemonService(PokemonRepository pokemonRepository, PokemonMarshaller pokemonMarshaller, TypeRepository typeRepository, MoveRepository moveRepository, PokemonSpeciesRepository pokemonSpeciesRepository, RandomPokemonStorage randomPokemonStorage) {
        this.pokemonRepository = pokemonRepository;
        this.pokemonMarshaller = pokemonMarshaller;
        this.typeRepository = typeRepository;
        this.moveRepository = moveRepository;
        this.pokemonSpeciesRepository = pokemonSpeciesRepository;
        this.randomPokemonStorage = randomPokemonStorage;
        this.listOfChagedPokemon = new ArrayList<>();
    }

    public List<PokemonDTO> getAll() {
        return pokemonRepository.findAll();
    }

    public Pokemon getById(int pokemonId) {
        return pokemonMarshaller.fromDTO(pokemonRepository.findById(pokemonId).orElse(null));
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

}
