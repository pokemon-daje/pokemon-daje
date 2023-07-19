package com.pokemon.daje.persistance.marshaller;

import com.pokemon.daje.model.Move;
import com.pokemon.daje.model.Pokemon;
import com.pokemon.daje.persistance.dto.MoveDTO;
import com.pokemon.daje.persistance.dto.PokemonDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class PokemonMarshaller implements
        BaseMarshaller<Pokemon, PokemonDTO> {

    MoveMarshaller moveMarshaller;

    @Autowired
    private PokemonMarshaller(MoveMarshaller moveMarshaller){
        this.moveMarshaller = moveMarshaller;
    }

    @Override
    public Pokemon fromDTO(PokemonDTO dto) {
        Pokemon pokemon = null;
        if (dto != null){
            pokemon = new Pokemon();
            pokemon.setId(dto.getId());
            pokemon.setName(dto.getName());
            Set<Move> moveSet = new HashSet<>();
            dto.getMoveSet().forEach(move -> moveSet.add(moveMarshaller.fromDTO(move)));
            pokemon.setMoves(moveSet);
        }
        return pokemon;
    }

    @Override
    public PokemonDTO toDTO(Pokemon business) {
        PokemonDTO pokemonDTO = null;
        if (business != null){
            pokemonDTO = new PokemonDTO();
            pokemonDTO.setId(business.getId());
            pokemonDTO.setName(business.getName());
            Set<MoveDTO> moveSet = new HashSet<>();
            business.getMoves().forEach(move -> moveSet.add(moveMarshaller.toDTO(move)));
            pokemonDTO.setMoveSet(moveSet);
        }
        return pokemonDTO;
    }
}
