package com.pokemon.daje.controller.json.marshaller;

import com.pokemon.daje.controller.json.dto.PokemonFrontEndDTO;
import com.pokemon.daje.model.Pokemon;
import com.pokemon.daje.persistance.marshaller.BaseMarshaller;
import org.springframework.stereotype.Component;

@Component
public class PokemonToFrontEndMarshaller implements BaseMarshaller<Pokemon, PokemonFrontEndDTO> {

    @Override
    public Pokemon fromDTO(PokemonFrontEndDTO dto) {
        Pokemon pokemon = null;
        if (dto != null){
            pokemon = new Pokemon();
            pokemon.setId(dto.getPokedexId());
            pokemon.setName(dto.getName());
            pokemon.setSpriteUrl(dto.getSpriteUrl());
            pokemon.setCurrentHP(dto.getCurrentHP());
            pokemon.setMaxHP(dto.getMaxHP());
            pokemon.setType(dto.getType());
            pokemon.setMoves(dto.getMoves());
            pokemon.setOriginalTrainer(dto.getOriginalTrainer());
        }
        return pokemon;
    }

    @Override
    public PokemonFrontEndDTO toDTO(Pokemon business) {
        PokemonFrontEndDTO pokemonDTO = null;
        if (business != null){
            pokemonDTO = new PokemonFrontEndDTO();
            pokemonDTO.setPokedexId(business.getId());
            pokemonDTO.setName(business.getName());
            pokemonDTO.setSpriteUrl(business.getSpriteUrl());
            pokemonDTO.setCurrentHP(business.getCurrentHP());
            pokemonDTO.setMaxHP(business.getMaxHP());
            pokemonDTO.setType(business.getType());
            pokemonDTO.setMoves(business.getMoves());
            pokemonDTO.setOriginalTrainer(business.getOriginalTrainer());
        }
        return pokemonDTO;
    }

    public PokemonFrontEndDTO toDTOWithDatabaseID(Pokemon business, int databaseId) {
        PokemonFrontEndDTO pokemonDTO = toDTO(business);
        if(pokemonDTO != null){
            pokemonDTO.setDatabaseId(databaseId);
        }
        return pokemonDTO;
    }
}
