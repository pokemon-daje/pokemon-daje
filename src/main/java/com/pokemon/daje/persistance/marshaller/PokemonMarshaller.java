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
            pokemon.setId(dto.getPokemonSpeciesDTO().getPokedexId());
            pokemon.setName(dto.getName());
            pokemon.setSpriteUrl(dto.getPokemonSpeciesDTO().getSpriteUrl());
            pokemon.setCurrentHP(dto.getCurrentHealthPoints());
            pokemon.setMaxHP(dto.getMaxHealthPoints());
            pokemon.setType(typeMarshaller.fromDTO(dto.getPokemonSpeciesDTO().getType()));

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
        if (business != null
                && business.getMoves() != null
                && !business.getMoves().isEmpty()
                && business.getType() != null
                && business.getType().getId() != null
                && !business.getMoves().stream().anyMatch(move -> move.getId() == null)){
            pokemonDTO = new PokemonDTO();
            pokemonDTO.setName(business.getName());
            pokemonDTO.setCurrentHealthPoints(business.getCurrentHP());
            pokemonDTO.setMaxHealthPoints(business.getMaxHP());

            pokemonDTO.getPokemonSpeciesDTO().setPokedexId(business.getId());
            pokemonDTO.getPokemonSpeciesDTO().setName(business.getName());
            pokemonDTO.getPokemonSpeciesDTO().setSpriteUrl(business.getSpriteUrl());

            Set<MoveDTO> moveSet = new HashSet<>();
            business.getMoves().forEach(move -> moveSet.add(moveMarshaller.toDTO(move)));
            pokemonDTO.setMoveSet(moveSet);

            pokemonDTO.setTrainerName(business.getOriginalTrainer());
        }
        return pokemonDTO;
    }
}
