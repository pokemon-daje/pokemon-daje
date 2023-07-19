package com.pokemon.daje.service;

import com.pokemon.daje.model.Pokemon;
import com.pokemon.daje.model.Types;
import com.pokemon.daje.persistance.dao.PokemonRepository;
import com.pokemon.daje.persistance.dao.PokemonSpeciesRepository;
import com.pokemon.daje.persistance.dao.TypeRepository;
import com.pokemon.daje.persistance.dto.MoveDTO;
import com.pokemon.daje.persistance.dto.PokemonDTO;
import com.pokemon.daje.persistance.dto.PokemonSpeciesDTO;
import com.pokemon.daje.persistance.dto.TypeDTO;
import com.pokemon.daje.persistance.marshaller.PokemonMarshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PokemonService {
    private final PokemonRepository pokemonRepository;
    private final PokemonMarshaller pokemonMarshaller;
    private final TypeRepository typeRepository;
    private final PokemonSpeciesRepository pokemonSpeciesRepository;

    @Autowired
    public PokemonService(PokemonRepository pokemonRepository, PokemonMarshaller pokemonMarshaller, TypeRepository typeRepository, PokemonSpeciesRepository pokemonSpeciesRepository) {
        this.pokemonRepository = pokemonRepository;
        this.pokemonMarshaller = pokemonMarshaller;
        this.typeRepository = typeRepository;
        this.pokemonSpeciesRepository = pokemonSpeciesRepository;
    }

    public List<PokemonDTO> getAll(){
        return pokemonRepository.findAll();
    }

    public Pokemon getById(int pokemonId){
        return pokemonMarshaller.fromDTO(pokemonRepository.findById(pokemonId).orElse(null));
    }

    public void insert(Pokemon pokemon){
        if(pokemon.getType() != null && !Types.fromString(pokemon.getType().getName()).equals(Types.UNKNOWN)) {
            PokemonDTO pokemonDTO = pokemonMarshaller.toDTO(pokemon);
            Optional<PokemonSpeciesDTO> optionalPokemonSpeciesDTO = pokemonSpeciesRepository.findById(pokemon.getId());
            optionalPokemonSpeciesDTO.ifPresent(pokemonDTO::setPokemonSpeciesDTO);
            Optional<TypeDTO> pokemonType = typeRepository.findByPokedexId(pokemonDTO.getPokemonSpeciesDTO().getType().getPokedexId());
            Set<MoveDTO> alteredMoves = new HashSet<>();
            pokemonDTO.getMoveSet().forEach(moveDTO -> {
                Optional<TypeDTO> optionalMoveType = typeRepository.findByPokedexId(moveDTO.getType().getPokedexId());
                optionalMoveType.ifPresent(moveType -> {
                    moveDTO.setType(moveType);
                    alteredMoves.add(moveDTO);
                });
            });
            if(pokemonType.isPresent() && alteredMoves.size() == pokemonDTO.getMoveSet().size()) {
                pokemonDTO.getPokemonSpeciesDTO().setType(pokemonType.get());
                pokemonRepository.save(pokemonDTO);
            }
        }
    }

    public void deleteById(int id){
        pokemonRepository.deleteById(id);
    }

    public List<Pokemon> getSixRandomPokemon() {
        List<PokemonDTO> pokemonDTOList = pokemonRepository.getSixRandomPokemon();
        return pokemonDTOList.stream().map(pokemonMarshaller::fromDTO).toList();
    }
}
