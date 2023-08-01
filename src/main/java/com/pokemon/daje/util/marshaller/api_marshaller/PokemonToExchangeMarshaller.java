package com.pokemon.daje.util.marshaller.api_marshaller;

import com.pokemon.daje.model.api_dto.PokemonExchangeDTO;
import com.pokemon.daje.model.Move;
import com.pokemon.daje.model.Pokemon;
import com.pokemon.daje.model.Type;
import com.pokemon.daje.util.marshaller.persistance.BaseMarshaller;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class PokemonToExchangeMarshaller implements BaseMarshaller<Pokemon, PokemonExchangeDTO> {

    @Override
    public Pokemon fromDTO(PokemonExchangeDTO dto) {
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
    public PokemonExchangeDTO toDTO(Pokemon business) {
        PokemonExchangeDTO pokemonDTO = null;
        if(business != null && business.getMoves() != null){
                pokemonDTO = new PokemonExchangeDTO();
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
