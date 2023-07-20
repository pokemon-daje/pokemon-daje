package com.pokemon.daje.controller;

import com.pokemon.daje.model.Pokemon;
import com.pokemon.daje.service.PokemonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PokemonController {

    final PokemonService pokemonService;
    @Autowired
    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }
    @GetMapping("/pokemons")
    public ResponseEntity<List<Pokemon>> getSixRandom(){
        return ResponseEntity.ok(pokemonService.getSixRandomPokemon());
    }
    @GetMapping("/pokemons/{id}")
    public ResponseEntity<Pokemon> getById(@PathVariable int id){
        return ResponseEntity.ok(pokemonService.getById(id));
    }
    @PostMapping("/pokemons")
    public void insert(@RequestBody Pokemon pokemon){
        pokemonService.insert(pokemon);
    }
    @DeleteMapping("/pokemons/{id}")
    public void delete(@PathVariable int id){
        pokemonService.deleteById(id);
    }
}
