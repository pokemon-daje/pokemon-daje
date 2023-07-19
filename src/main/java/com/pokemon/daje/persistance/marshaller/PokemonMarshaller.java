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

    private final MoveMarshaller moveMarshaller;
    private final TypeMarshaller typeMarshaller;

    @Autowired
    private PokemonMarshaller(MoveMarshaller moveMarshaller, TypeMarshaller typeMarshaller){
        this.moveMarshaller = moveMarshaller;
        this.typeMarshaller = typeMarshaller;
    }

    @Override
    public Pokemon fromDTO(PokemonDTO dto) {
        Pokemon pokemon = null;
        if (dto != null){
            pokemon = new Pokemon();
            pokemon.setId(dto.getId());
            pokemon.setName(dto.getName());
            pokemon.setSpriteUrl(dto.getSpriteUrl());
            pokemon.setCurrentHP(dto.getCurrentHealthPoints());
            pokemon.setMaxHP(dto.getMaxHealthPoints());
            pokemon.setType(typeMarshaller.fromDTO(dto.getType()));
            Set<Move> moveSet = new HashSet<>();
            dto.getMoveSet().forEach(move -> moveSet.add(moveMarshaller.fromDTO(move)));
            pokemon.setMoves(moveSet);
            pokemon.setOriginalTrainer(dto.getTrainerName());
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
            pokemonDTO.setSpriteUrl(business.getSpriteUrl());
            pokemonDTO.setCurrentHealthPoints(business.getCurrentHP());
            pokemonDTO.setMaxHealthPoints(business.getMaxHP());
            pokemonDTO.setType(typeMarshaller.toDTO(business.getType()));
            Set<MoveDTO> moveSet = new HashSet<>();
            business.getMoves().forEach(move -> moveSet.add(moveMarshaller.toDTO(move)));
            pokemonDTO.setMoveSet(moveSet);
            pokemonDTO.setTrainerName(business.getOriginalTrainer());
        }
        return pokemonDTO;
    }
}
