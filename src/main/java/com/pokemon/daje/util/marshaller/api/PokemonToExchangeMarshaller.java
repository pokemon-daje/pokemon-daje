package com.pokemon.daje.util.marshaller.api;

import com.pokemon.daje.model.api_objects.PokemonRequestExchangeDTO;
import com.pokemon.daje.model.business_data.Move;
import com.pokemon.daje.model.business_data.Pokemon;
import com.pokemon.daje.model.business_data.Type;
import com.pokemon.daje.util.marshaller.persistance.BaseMarshaller;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class PokemonToExchangeMarshaller implements BaseMarshaller<Pokemon, PokemonRequestExchangeDTO> {

    @Override
    public Pokemon fromDTO(PokemonRequestExchangeDTO dto) {
        Pokemon pokemon = null;
        if(dto != null && dto.getMoves() != null){
                pokemon = new Pokemon();
                pokemon.setId(dto.getId());
                pokemon.setName(dto.getName());
                pokemon.setCurrentHP(dto.getCurrentHP());
                pokemon.setType(new Type(dto.getType()));
                pokemon.setMaxHP(dto.getMaxHP());
                pokemon.setOriginalTrainer(dto.getOriginalTrainer());
                pokemon.setMoves(dto.getMoves().stream().map(Move::new).collect(Collectors.toSet()));
        }
        return pokemon;
    }

    @Override
    public PokemonRequestExchangeDTO toDTO(Pokemon business) {
        PokemonRequestExchangeDTO pokemonDTO = null;
        if(business != null && business.getMoves() != null){
                pokemonDTO = new PokemonRequestExchangeDTO();
                pokemonDTO.setId(business.getId());
                pokemonDTO.setName(business.getName());
                pokemonDTO.setType(business.getId());
                pokemonDTO.setMoves(business.getMoves().stream().map(Move::getId).collect(Collectors.toSet()));
                pokemonDTO.setCurrentHP(business.getCurrentHP());
                pokemonDTO.setMaxHP(business.getMaxHP());
                pokemonDTO.setOriginalTrainer(business.getOriginalTrainer());
        }
        return pokemonDTO;
    }
}
