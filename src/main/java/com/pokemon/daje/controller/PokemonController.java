package com.pokemon.daje.controller;

import com.pokemon.daje.model.Pokemon;
import com.pokemon.daje.service.PokemonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Takes six Pokemon from the database randomly")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ok"),
            @ApiResponse(responseCode = "400", description = "bad request"),
            @ApiResponse(responseCode = "500", description = "internal server error"),
    })
    @GetMapping("/pokemons")
    public ResponseEntity<List<Pokemon>> getSixRandom() {
        return ResponseEntity.ok(pokemonService.getSixRandomPokemon());
    }
    @Operation(summary = "Get Pokemon by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ok"),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied"),
            @ApiResponse(responseCode = "500", description = "internal server error"),
    })
    @GetMapping("/pokemons/{id}")
    public ResponseEntity<Pokemon> getById(@Parameter(description = "id of the pokemon to be searched")@PathVariable int id) {
        return ResponseEntity.ok(pokemonService.getById(id));
    }

    @Operation(summary = "Create a new Pokemon")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ok"),
            @ApiResponse(responseCode = "500", description = "internal server error",content = @Content),
    })
    @PostMapping("/pokemons")
    public void insert(@RequestBody Pokemon pokemon) {
        pokemonService.insert(pokemon);
    }

    @Operation(summary = "Pokemon exchange")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ok"),
            @ApiResponse(responseCode = "500", description = "internal server error",
                    content = @Content),
    })
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

    @Operation(summary = "This method returns a Server-Sent Events (SSE) stream of the list of changed Pokemon")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The SSE stream is returned", content = @Content),
            @ApiResponse(responseCode = "400", description = "bad request"),
            @ApiResponse(responseCode = "500", description = "internal server error"),
    })
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


    @Operation(summary = "Delete Pokemon by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok",content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
    })
    @DeleteMapping("/pokemons/{id}")
    public void delete(@Parameter(description = "id of the pokemon to be deleted")
                        @PathVariable int id) {
        pokemonService.deleteById(id);
    }
}
