package com.pokemon.daje.service;

import com.pokemon.daje.model.Pokemon;
import com.pokemon.daje.persistance.dao.PokemonRepository;
import com.pokemon.daje.persistance.dto.PokemonDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PokemonService {
    final PokemonRepository pokemonRepository;

    @Autowired
    public PokemonService(PokemonRepository pokemonRepository) {
        this.pokemonRepository = pokemonRepository;
    }

    public List<PokemonDTO> getAll(){
        return pokemonRepository.findAll();
    }

    public PokemonDTO getById(int pokemonId){
        return pokemonRepository.getReferenceById(pokemonId);
    }

    public void insert(Pokemon pokemon){
         //pokemonRepository.save(pokemon);
    }

    public void deleteById(int id){
        pokemonRepository.deleteById(id);
    }
}
