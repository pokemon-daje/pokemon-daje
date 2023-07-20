package com.pokemon.daje.controller;

import com.pokemon.daje.model.Pokemon;
import com.pokemon.daje.service.PokemonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api")
public class PokemonController {
    final PokemonService pokemonService;
    private Set<SseEmitter> serverEmitters;

    @Autowired
    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
        serverEmitters =new HashSet<>();
    }

    @GetMapping("/pokemons")
    public ResponseEntity<List<Pokemon>> getSixRandom() {
        return ResponseEntity.ok(pokemonService.getSixRandomPokemon());
    }

    @GetMapping("/pokemons/{id}")
    public ResponseEntity<Pokemon> getById(@PathVariable int id) {
        return ResponseEntity.ok(pokemonService.getById(id));
    }

    @PostMapping("/pokemons")
    public void insert(@RequestBody Pokemon pokemon) {
        pokemonService.insert(pokemon);
    }

    @PostMapping("/pokemons/swap")
    public Pokemon swap(@RequestBody Pokemon pokemon) {
        Pokemon toSend = pokemonService.swap(pokemon);
        List<SseEmitter> usedEmitter = new ArrayList<>();
        serverEmitters.forEach(sseEmitter -> {
                    try {
                        sseEmitter.send(SseEmitter.event()
                                .data(pokemonService.getListOfChagedPokemon())
                                .id("exchange")
                                .name("pokemon"));
                        usedEmitter.add(sseEmitter);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        usedEmitter.forEach(ResponseBodyEmitter::complete);
        return toSend;
    }

    @GetMapping("/pokemons/exchange")
    public SseEmitter streamSseMvc() {
        SseEmitter emitter = new SseEmitter(2000L);
            try {
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .data(pokemonService.getListOfChagedPokemon())
                        .id("exchange")
                        .name("pokemon");
                emitter.send(event);

                serverEmitters.add(emitter);
                emitter.onCompletion(()->{serverEmitters.remove(emitter);});
                emitter.onTimeout(emitter::complete);

            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        return emitter;
    }

    @DeleteMapping("/pokemons/{id}")
    public void delete(@PathVariable int id) {
        pokemonService.deleteById(id);
    }

}
